package org.cuair.ground.controllers.target;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import org.apache.coyote.Response;
import org.cuair.ground.clients.AutopilotClient;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.AlphanumTargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Controller to handle creation/retrieval of Alphanumeric Target model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/alphanum_target")
public class AlphanumTargetController extends TargetController<AlphanumTarget> {

  /** Database accessor object for alphanum targets */
  private static final AlphanumTargetDatabaseAccessor<AlphanumTarget> targetDao =
      (AlphanumTargetDatabaseAccessor<AlphanumTarget>) DAOFactory.getDAO(
          DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

  /** Database accessor object for alphanum target sightings */
  private static final AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting> targetSightingsDao =
          (AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting>) DAOFactory.getDAO(
                  DAOFactory.ModelDAOType.ALPHANUM_TARGET_SIGHTINGS_DATABASE_ACCESSOR, AlphanumTargetSighting.class);

  /** Gets the database accessor object for this target */
  public AlphanumTargetDatabaseAccessor<AlphanumTarget> getTargetDao() {
    return targetDao;
  }

  /**
   * Constructs an HTTP response with all the targets
   *
   * @return a list of all alphanum targets in the db
   */
  @Override
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity getAll() {
    return super.getAll();
  }

  /**
   * Create Target
   *
   * @return the created alphanum target on success, 400 when the request includes an id or creator field
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity create(@RequestBody AlphanumTarget t) {
    return super.create(t);
  }

  /**
   * Update Target by id
   *
   * @param id Long id of the Target being updated
   * @return the updated alphanum target on success, 204 when the alphanum target with the specified id does not exist,
   * or 400 when the request includes an id or creator field
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResponseEntity update(@PathVariable Long id, @RequestBody AlphanumTarget other) {
    AlphanumTarget t = targetDao.get(id);
    if (t == null) {
      return notFound().build();
    }
    return super.update(t, other);
  }

  @RequestMapping(value="/{id}/geotags", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity update(@PathVariable Long id, @RequestBody List<Long> ids) {
    AlphanumTarget t = targetDao.get(id);
    if (t == null) {
      return notFound().build();
    }
    t.setGeotag(medianFromTsIds(ids));
    targetDao.update(t);
    return ok(t);
  }

  public static Geotag medianFromTsIds(List<Long> ids) {
    Geotag[] geotags = new Geotag[ids.size()];
    for (int i = 0; i < ids.size(); i++) {
      TargetSighting ts = getTargetSightingDao().get(ids[i]);
      geotags[i] = ts.getGeotag();
    }
    return Geotagging.median(Arrays.stream(geotag));
  }

  /**
   * Deletes a Target and unassigns all TargetSightings that were assigned to this Target You must
   * send an empty body to do a delete.
   *
   * @param id Long id of target to be deleted
   * @return the deleted alphanum target
   */
  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity delete(@PathVariable Long id) {
    return super.delete(id);
  }


  /**
   * Deletes all alphanum targets in the database
   * @return the list of deleted alphanum targets
   */
  @RequestMapping(method = RequestMethod.DELETE)
  public ResponseEntity deleteAll() {
    List<AlphanumTarget> targets = targetDao.getAll();
    for (AlphanumTarget t : targets) {
      long id = t.getId();
      targetSightingsDao.unassociateAllTargetSightingsForTarget(id);
      targetDao.delete(id);
    }
    return ok(targets);
  }

  /**
   * Calls method to send geolocation data to autopilot
   * @param t AlphanumTarget
   * @return sent target
   */
  @RequestMapping(value = "/send", method = RequestMethod.POST)
  public ResponseEntity sendTarget(@RequestBody AlphanumTarget t) {
    AutopilotClient ac = new AutopilotClient();
    ac.sendTargetData(t);
    return ok(t);

  }
}

