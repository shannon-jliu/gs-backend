package org.cuair.ground.controllers.target;

import java.util.List;

import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetDatabaseAccessor;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.plane.target.EmergentTarget;
import org.cuair.ground.models.plane.target.EmergentTargetSighting;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Controller to handle Emergent Target sightings model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/emergent_target_sighting")
public class EmergentTargetSightingController
    extends TargetSightingController<EmergentTargetSighting> {

  /** Database accessor object for emergent target sightings */
  private static final TargetSightingsDatabaseAccessor<EmergentTargetSighting> eSightingDao =
      (TargetSightingsDatabaseAccessor<EmergentTargetSighting>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
              EmergentTargetSighting.class);

  /** Database accessor object for emergent targets */
  private static final TargetDatabaseAccessor<EmergentTarget> eTargetDao =
      (TargetDatabaseAccessor<EmergentTarget>)
          DAOFactory.getDAO(DAOFactory.ModelDAOType.TARGET_DATABASE_ACCESSOR, EmergentTarget.class);

  @Override
  TargetSightingsDatabaseAccessor<EmergentTargetSighting> getTargetSightingDao() {
    return eSightingDao;
  }

  @Override
  protected String getTypeName() {
    return "emergent";
  }


  @Override
  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public List<EmergentTargetSighting> getAll() {
    return super.getAll();
  }

  @Override
  @RequestMapping(value = "/assignment/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public EmergentTargetSighting create(@PathVariable Long id, @RequestBody EmergentTargetSighting ts) {
    // TODO: Fix: This threw a NullPointerException when moving a target sighting to a target. Neither were emergent though
    // The console on the frontend also threw an error: "index.js:1437 Warning: Can't perform a React state update on an unmounted component. This is a no-op, but it indicates a memory leak in your application. To fix, cancel all subscriptions and asynchronous tasks in the componentWillUnmount method.
    // in MergeSightingPreview (at mergeTarget.js:314)""
    EmergentTarget t = eTargetDao.getAll().get(0);
    if (ts.getCreator().getUserType() != ODLCUser.UserType.MDLCTAGGER && ts.getCreator().getUserType() != ODLCUser.UserType.MDLCOPERATOR) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only MDLC should be creating Emergent Target Sightings");
    }
    ts.setTarget(t);
    return super.create(id, ts);
  }

  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public EmergentTargetSighting update(@PathVariable Long id, @RequestBody EmergentTargetSighting other) {
    if (other.getTarget() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't pass targets for emergent target sighting update");
    }
    EmergentTargetSighting ts = eSightingDao.get(id);
    if (ts == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return updateFromTargetSighting(ts, other);
  }

  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public void delete(@PathVariable Long id) {
    EmergentTargetSighting ts = eSightingDao.get(id);
    if (ts == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    deleteFromSighting(ts);

    if (ts.getTarget() != null && ts.getTarget().getthumbnailTsid().equals(id)) {
      ts.getTarget().setthumbnailTsid(0L);
      eTargetDao.update(ts.getTarget());
    }
  }
}
