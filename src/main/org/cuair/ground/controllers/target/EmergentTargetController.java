package org.cuair.ground.controllers.target;

import static org.springframework.http.ResponseEntity.notFound;

import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.plane.target.EmergentTarget;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Controller to handle creation/retrieval of Emergent Target model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/emergent_target")
public class EmergentTargetController extends TargetController<EmergentTarget> {

  /** Database accessor object for emergent targets */
  private static final ClientCreatableDatabaseAccessor<EmergentTarget> targetDao =
      (ClientCreatableDatabaseAccessor<EmergentTarget>) DAOFactory.getDAO(
          DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget.class);

  /** Gets the database accessor object for this target */
  @Override
  public ClientCreatableDatabaseAccessor<EmergentTarget> getTargetDao() {
    return targetDao;
  }

  /**
   * Constructs an HTTP response with all the targets
   *
   * @return a list of all emergent targets in the db
   */
  @Override
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity getAll() {
    return super.getAll();
  }

  /**
   * Create Target
   *
   * @return the created emergent target on success, 400 when the request includes an id or creator field
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity create(@RequestBody EmergentTarget t) {
    return super.create(t);
  }

  /**
   * Update Target by id
   *
   * @param id Long id of the Target being updated
   * @return the updated emergent target on success, 204 when the emergent target with the specified id does not exist,
   * or 400 when the request includes an id or creator field
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResponseEntity update(@PathVariable Long id, @RequestBody EmergentTarget other) {
    EmergentTarget t = targetDao.get(id);
    if (t == null) {
      return notFound().build();
    }
    return super.update(t, other);
  }

  /**
   * Deletes a Target and unassigns all TargetSightings that were assigned to this Target You must
   * send an empty body to do a delete.
   *
   * @param id Long id of target to be deleted
   * @return the deleted emergent target
   */
  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity delete(@PathVariable Long id) {
    return super.delete(id);
  }
}
