package org.cuair.ground.controllers.target;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;

import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.AlphanumTargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;
import org.cuair.ground.util.Flags;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Controller to handle Alphanumeric Target sightings model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/alphanum_target_sighting")
public class AlphanumTargetSightingController
    extends TargetSightingController<AlphanumTargetSighting> {

  private static final AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting>
      alphaTargetSightingDao =
      (AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.ALPHANUM_TARGET_SIGHTINGS_DATABASE_ACCESSOR,
              AlphanumTargetSighting.class);
  private static final AlphanumTargetDatabaseAccessor<AlphanumTarget> alphaTargetDao =
      (AlphanumTargetDatabaseAccessor<AlphanumTarget>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);
  private boolean cuairInteropRequests = Flags.CUAIR_INTEROP_REQUESTS;

  /**
   * Gets the TargetSightingsDatabaseAccessor object for this target sighting
   *
   * @return the alphanum target sighting database accessor object
   */
  @Override
  TargetSightingsDatabaseAccessor<AlphanumTargetSighting> getTargetSightingDao() {
    return alphaTargetSightingDao;
  }

  /**
   * Constructs an HTTP response with all the target sightings
   *
   * @return a list of all alphanum target sightings in the db
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity getAll() {
    return super.getAll();
  }

  /**
   * Creates an alphanumeric target sighting in this assigned image
   *
   * @param id the id of the assignment for which to create target sighting
   * @return the created alphanum target sighting on success, 204 when the associated assignment
   * does not exist, or 400 when the request includes a target, id, or geotag field or if the
   * creator field does not match the creator of the associated assignment
   */
  @RequestMapping(value = "/assignment/{id}", method = RequestMethod.POST)
  public ResponseEntity create(@PathVariable Long id, @RequestBody AlphanumTargetSighting ts) {
    if (ts.isOffaxis() != null && ts.isOffaxis()) {
      if (ts.getTarget() != null) {
        return badRequest().body("Don't pass targets for off-axis sighting creates");
      }
      ts.setTarget(alphaTargetDao.getOffaxisTarget());
    }

    final ResponseEntity retval = super.create(id, ts);

    if (retval.getStatusCodeValue() == 200) {
      if (ts.getCreator().getUserType() == ODLCUser.UserType.ADLC) {
        // Sets new target thumbnail
        if (ts.getTarget() != null) {
          AlphanumTarget t = alphaTargetDao.get(ts.getTarget().getId());
          t.setthumbnailTsid(ts.getId());
          alphaTargetDao.update(t);
          if (cuairInteropRequests) {
            // TODO: Add back in once client code is complete
            // interopClient.updateTargetImage(ts);
          }
        }
      }
    }
    return retval;
  }

  /**
   * Constructs an HTTP response after updating an AlphanumTargetSighting given an id
   *
   * @param id Long id of target sighting
   * @return the updated alphanum target sighting on success, 204 if the alphanum target sighting
   * corresponding to the provided id does not exist, or 400 if supplied target sighting with updated
   * fields is incorrectly formatted
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResponseEntity update(@PathVariable Long id, @RequestBody AlphanumTargetSighting other) {
    AlphanumTargetSighting ts = alphaTargetSightingDao.get(id);
    if (ts == null) {
      return notFound().build();
    }

    // checks if the target is offaxis or updated to offaxis and if a target is set
    if (((ts.isOffaxis() && other.isOffaxis() == null) || (Boolean.TRUE.equals(other.isOffaxis())))
        && other.getTarget() != null) {
      return badRequest().body("Don't pass targets for off-axis sighting updates");
    }

    if (Boolean.TRUE.equals(other.isOffaxis())) {
      other.setTarget(alphaTargetDao.getOffaxisTarget());
    }

    // Saved for later because if target is ADLC, we want to wait for the ts to be erased from the
    // db
    // checks whether thumbnail for current target should be saved
    boolean toEraseThumb =
        ts.getTarget() != null
            && (other.getTarget() == null
            || !ts.getTarget().getId().equals(other.getTarget().getId()))
            && ts.getTarget().getthumbnailTsid() != null
            && ts.getTarget().getthumbnailTsid().equals(ts.getId());
    AlphanumTarget tToEraseFrom = ts.getTarget();
    // checks whether new thumb for new target should be updated
    boolean updateNewThumb =
        other.getTarget() != null
            && (ts.getTarget() == null
            || !ts.getTarget().getId().equals(other.getTarget().getId()));

    final ResponseEntity retval = updateFromTargetSighting(ts, other);

    if (retval.getStatusCodeValue() == 200) {
      if (toEraseThumb) {
        if (ts.getCreator().getUserType() == ODLCUser.UserType.MDLCTAGGER
            || ts.getCreator().getUserType() == ODLCUser.UserType.MDLCOPERATOR) {
          // If MDLC, set thumb for original target to default value
          tToEraseFrom.setthumbnailTsid(0L);
        } else {
          // If ADLC, set thumb for original target to most recent ts (or default if none)
          AlphanumTargetSighting newThumb =
              alphaTargetSightingDao.getLastSightingForTarget(tToEraseFrom.getId());
          if (newThumb != null) {
            newThumb.setTarget(tToEraseFrom);
            tToEraseFrom.setthumbnailTsid(newThumb.getId());
            if (cuairInteropRequests) {
              // TODO: Add back in once client code is complete
              // interopClient.updateTargetImage(newThumb);
            }
          } else {
            tToEraseFrom.setthumbnailTsid(0L);
          }
        }
        alphaTargetDao.update(tToEraseFrom);
      }

      if (ts.getCreator() != null && ts.getCreator().getUserType() == ODLCUser.UserType.ADLC) {
        // Sets new thumbnail for updated target
        if (updateNewThumb) {
          AlphanumTarget t = alphaTargetDao.get(ts.getTarget().getId());
          t.setthumbnailTsid(ts.getId());
          alphaTargetDao.update(t);
          if (cuairInteropRequests) {
            // TODO: Add back in once client code is complete
            // interopClient.updateTargetImage(ts);
          }
        }
      }
    }
    return retval;
  }

  /**
   * Constructs an HTTP response after deleting a TargetSighting given an id
   *
   * @param id Long id of target sighting
   * @return the deleted alphanum target sighting on success, 204 when the alphanum
   * target sighting corresponding to the provided id does not exist
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity delete(@PathVariable Long id) {
    AlphanumTargetSighting ts = alphaTargetSightingDao.get(id);
    if (ts == null) {
      return notFound().build();
    }

    final ResponseEntity retval = super.delete(ts);

    if (ts.getTarget() != null && ts.getTarget().getthumbnailTsid() != null
        && ts.getTarget().getthumbnailTsid().equals(id)) {
      if (ts.getCreator().getUserType() == ODLCUser.UserType.MDLCTAGGER
          || ts.getCreator().getUserType() == ODLCUser.UserType.MDLCOPERATOR) {
        // If MDLC, set thumb for original target to default value
        ts.getTarget().setthumbnailTsid(0L);
      } else {
        // If ADLC, set thumb for original target to most recent ts (or default if none)
        AlphanumTargetSighting newThumb =
            alphaTargetSightingDao.getLastSightingForTarget(ts.getTarget().getId());
        if (newThumb != null) {
          ts.getTarget().setthumbnailTsid(newThumb.getId());
          if (cuairInteropRequests) {
            // TODO: Add back in once client code is complete
            // interopClient.updateTargetImage(newThumb);
          }
        } else {
          ts.getTarget().setthumbnailTsid(0L);
        }
      }
      alphaTargetDao.update(ts.getTarget());
    }
    return retval;
  }
}
