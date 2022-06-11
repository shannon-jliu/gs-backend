package org.cuair.ground.controllers.target;

import static org.springframework.http.ResponseEntity.notFound;

import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle creation/retrieval of Alphanumeric Target model objects
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/alphanum_target")
public class AlphanumTargetController extends TargetController<AlphanumTarget> {

  /** Database accessor object for alphanum targets */
  private static final AlphanumTargetDatabaseAccessor<AlphanumTarget> targetDao = (AlphanumTargetDatabaseAccessor<AlphanumTarget>) DAOFactory
      .getDAO(
          DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

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
   * @return the created alphanum target on success, 400 when the request includes
   *         an id or creator field
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity create(@RequestBody AlphanumTarget t) {
    return super.create(t);
  }

  /**
   * Update Target by id
   *
   * @param id Long id of the Target being updated
   * @return the updated alphanum target on success, 204 when the alphanum target
   *         with the specified id does not exist,
   *         or 400 when the request includes an id or creator field
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResponseEntity update(@PathVariable Long id, @RequestBody AlphanumTarget other) {
    AlphanumTarget t = targetDao.get(id);
    if (t == null) {
      return notFound().build();
    }
    return super.update(t, other);
  }

  /**
   * Deletes a Target and unassigns all TargetSightings that were assigned to this
   * Target You must
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
   * Defines endpoint for Intelligent Systems to submit an autonomous target.
   *
   * @param jsonString the json representing the target submission data
   * @param rawThumb   the bytes representing the target thumbnail
   *
   * @return 200 on success, 400 when request parts don't exist,
   *         or 500 if communication with interop fails or the file cannot be
   *         parsed.
   */
  @RequestMapping(value = "/auto", method = RequestMethod.POST)
  public ResponseEntity submitAuto(@RequestPart("json") String jsonString, @RequestPart("bytes") byte[] rawThumb) {
    if (jsonString == null) {
      return badRequest().body("Missing target json");
    }
    if (rawThumb == null) {
      return badRequest().body("Missing target thumbnail");
    }

    // Check to see if the image is small enough -> otherwise interop will fail
    if (rawThumb.length > Math.pow(10, 6))
      return badRequest().body("Thumbnail file is larger than one megabyte.");

    // Send to interop
    if (Flags.CUAIR_INTEROP_REQUESTS) {
      if (!interopClient.submitTargetAndThumb(jsonString, rawThumb)) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Interop submission failed.");
      }
    }

    return ok(jsonString);
  }
}
