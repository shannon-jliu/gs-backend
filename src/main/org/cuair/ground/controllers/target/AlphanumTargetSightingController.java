package org.cuair.ground.controllers.target;

import java.util.List;

import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.AlphanumTargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;
import org.cuair.ground.util.Flags;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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


  @Override
  TargetSightingsDatabaseAccessor<AlphanumTargetSighting> getTargetSightingDao() {
    return alphaTargetSightingDao;
  }

  @Override
  protected String getTypeName() {
    return "alphanum";
  }


  @Override
  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public List<AlphanumTargetSighting> getAll() {
    return super.getAll();
  }

  @Override
  @RequestMapping(value = "/assignment/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public AlphanumTargetSighting create(@PathVariable Long id, @RequestBody AlphanumTargetSighting ts) {
    if (ts.isOffaxis() != null && ts.isOffaxis()) {
      if (ts.getTarget() != null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't pass targets for off-axis sighting creates");
      }
      ts.setTarget(alphaTargetDao.getOffaxisTarget());
    }

    // ordered this way so exception interrupts execution
    final AlphanumTargetSighting retval = super.create(id, ts);

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
    return retval;
  }

  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public AlphanumTargetSighting update(@PathVariable Long id, @RequestBody AlphanumTargetSighting other) {
    AlphanumTargetSighting ts = alphaTargetSightingDao.get(id);
    if (ts == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    // checks if the target is offaxis or updated to offaxis and if a target is set
    if (((ts.isOffaxis() && other.isOffaxis() == null) || (Boolean.TRUE.equals(other.isOffaxis())))
        && other.getTarget() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't pass targets for off-axis sighting updates");
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

    final AlphanumTargetSighting retval = updateFromTargetSighting(ts, other);

    if (toEraseThumb) {
      if (ts.getCreator().getUserType() == ODLCUser.UserType.MDLCTAGGER
          || ts.getCreator().getUserType() == ODLCUser.UserType.MDLCOPERATOR) {
        // If MDLC, set thumb for original target to default value
        tToEraseFrom.setthumbnailTsid(0L);
        // TODO (mariasam1): delete thumbnail through interop
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

    if (ts.getCreator().getUserType() == ODLCUser.UserType.ADLC) {
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
    return retval;
  }

  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public void delete(@PathVariable Long id) {
    AlphanumTargetSighting ts = alphaTargetSightingDao.get(id);
    if (ts == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    deleteFromSighting(ts);

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
  }
}
