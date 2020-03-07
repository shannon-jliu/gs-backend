package org.cuair.ground.controllers.target;

import org.cuair.ground.clients.ClientFactory;
import org.cuair.ground.clients.AutopilotClient;
import org.cuair.ground.clients.InteropClient;

import org.springframework.http.ResponseEntity;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.models.plane.target.TargetSighting;
import org.cuair.ground.util.Flags;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cuair.ground.util.Flags;


/** Controller to handle TargetSighting model objects */
public abstract class TargetSightingController<T extends TargetSighting> {

  /** The database access object for the assignment table */
  private static final AssignmentDatabaseAccessor assignmentDao =
      (AssignmentDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
  private boolean CUAIR_INTEROP_REQUESTS = Flags.CUAIR_INTEROP_REQUESTS;
  private boolean cuairGeotagMutable = Flags.CUAIR_GEOTAG_MUTABLE;

  protected static InteropClient interopClient = ClientFactory.getInteropClient();

  protected static final AutopilotClient autopilotClient = ClientFactory.getAutopilotClient();

  /** Gets the database accessor object for this target sighting */
  abstract TargetSightingsDatabaseAccessor<T> getTargetSightingDao();

  /**
   * Constructs an HTTP response with all the target sightings
   *
   * @return HTTP response with the json of all the target sightings
   */
  public ResponseEntity getAll() {
    return ok(getTargetSightingDao().getAll());
  }

  /**
   * Creates a target sighting in this assigned image
   *
   * @param assignmentId the id of the assignment for which to create target sighting
   * @return ResponseEntity containing the newly created target sighting as json
   */
  public ResponseEntity create(Long assignmentId, T ts) {
    Assignment a = assignmentDao.get(assignmentId);
    if (a == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    if (ts.getId() != null) {
      return badRequest().body("Don't pass in ids for creates");
    }
    if (ts.getGeotag() != null) {
      return badRequest().body("Don't pass geotag for creates");
    }
    if (ts.getCreator() != a.getAssignee()) {
      return badRequest().body("Creator client type does not match " + "client type of assignment");
    }
    ts.setAssignment(a);

    boolean geotagChanged = Geotag.attemptSetGeotagForTargetSighting(ts);
    getTargetSightingDao().create(ts);

    if (geotagChanged && ts.getTarget() != null) {
      // Updates target in dao
      Geotag.updateGeotag(ts.getTarget(), ts);

      if (CUAIR_INTEROP_REQUESTS) {
        interopClient.attemptUpdate(ts.getTarget());
      }
    }

    // TODO: Add back in once client code is complete
    // if (ts.getCreator().getUserType() == ODLCUser.UserType.MDLCTAGGER || ts.getCreator().getUserType() == ODLCUser.UserType.MDLCOPERATOR)
    // autopilotClient.sendMdlcRoi(AllTargetSightingController.Companion.getConfidenceGeotags());
    return ok(ts);
  }

  /**
   * Constructs an HTTP response after updating a target sighting given an id
   *
   * @param id Long id of target sighting
   * @return HTTP response with json of updated target sighting
   */
  public abstract ResponseEntity update(Long id, T other);

  /**
   * Constructs an HTTP response after updating a target sighting given an updated target sighting
   *
   * @param ts    Target sighting to be updated
   * @param other Target sighting will updated fields
   * @return HTTP response with json of updated target sighting
   */
  ResponseEntity updateFromTargetSighting(T ts, T other) {
    if (other.getId() != null) {
      return badRequest().body("Don't pass ids for updates");
    }
    if (other.getCreator() != null) {
      return badRequest().body("Don't pass creator for updates");
    }
    if (other.getGeotag() != null) {
      return badRequest().body("Don't pass geotag for updates");
    }

    if (other.getpixelX() != null && !other.getpixelX().equals(ts.getpixelX())) {
      return badRequest().body("Don't change value of pixel_x. Current value is " + ts.getpixelX());
    }
    if (other.getpixelY() != null && !other.getpixelY().equals(ts.getpixelY())) {
      return badRequest().body("Don't change value of pixel_y. Current value is " + ts.getpixelY());
    }
    if (other.getWidth() != null && !other.getWidth().equals(ts.getWidth())) {
      return badRequest().body("Don't change value of width. Current value is " + ts.getWidth());
    }
    if (other.getHeight() != null && !other.getHeight().equals(ts.getHeight())) {
      return badRequest().body("Don't change value of height. Current value is " + ts.getHeight());
    }
    ts.updateFromTargetSighting(other);

    boolean geotagChanged = cuairGeotagMutable && Geotag.attemptSetGeotagForTargetSighting(ts);

    getTargetSightingDao().update(ts);

    if (geotagChanged && other.getTarget() != null) {
      Geotag.updateGeotag(ts.getTarget(), null);
      if (CUAIR_INTEROP_REQUESTS) {
        // TODO: Add back in once client code is complete
        interopClient.attemptUpdate(ts.getTarget());
      }
    }
    // TODO: Add back in once client code is complete
    // if (ts.getCreator().getUserType() == ODLCUser.UserType.MDLCTAGGER || ts.getCreator().getUserType() == ODLCUser.UserType.MDLCOPERATOR)
    // autopilotClient.sendMdlcRoi(AllTargetSightingController.Companion.getConfidenceGeotags());
    return ok(ts);
  }

  /**
   * Constructs an HTTP response after deleting a TargetSighting
   *
   * @param ts Target sighting
   * @return HTTP response with json of deleted target sighting
   */
  public ResponseEntity delete(T ts) {
    getTargetSightingDao().delete(ts.getId());

    if (ts.getTarget() != null) {
      Geotag.updateGeotag(ts.getTarget(), null);
      if (CUAIR_INTEROP_REQUESTS) {
        // TODO: Add back in once client code is complete
        interopClient.attemptUpdate(ts.getTarget());
      }
    }
    // TODO: Add back in once client code is complete
    // if (ts.getCreator().UserType == ODLCUser.UserType.ADLC)
    // autopilotClient.sendMdlcRoi(AllTargetSightingController.Companion.getConfidenceGeotags());
    return ok(ts);
  }
}
