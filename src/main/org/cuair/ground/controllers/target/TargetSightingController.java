package org.cuair.ground.controllers.target;

// TODO: Add back in once client code is complete
// import org.cuair.ground.clients.ClientFactory;
// import org.cuair.ground.clients.InteropClient;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.models.plane.target.TargetSighting;
import org.cuair.ground.util.Flags;
import org.springframework.http.ResponseEntity;

/** Controller to handle TargetSighting model objects */
public abstract class TargetSightingController<T extends TargetSighting> {

  /** The database access object for the assignment table */
  private static final AssignmentDatabaseAccessor assignmentDao =
      (AssignmentDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
  private boolean cuairInteropRequests = Flags.CUAIR_INTEROP_REQUESTS;

  // TODO: Add back in once client code is complete, and add back the documentation
  // static InteropClient interopClient = ClientFactory.getInteropClient();

  /** Gets the database accessor object for this target sighting */
  abstract TargetSightingsDatabaseAccessor<T> getTargetSightingDao();

  /**
   * Constructs an HTTP response with all the target sightings
   *
   * @return a list of all target sightings in the db
   */
  public ResponseEntity getAll() {
    return ok(getTargetSightingDao().getAll());
  }

  /**
   * Creates a target sighting in this assigned image
   *
   * @param assignmentId the id of the assignment for which to create target sighting
   * @return the created target sighting on success, 204 when the associated assignment
   * does not exist, or 400 when the request includes an id or geotag field or if the
   * creator field does not match the creator of the associated assignment
   */
  public ResponseEntity create(Long assignmentId, T ts) {
    Assignment a = assignmentDao.get(assignmentId);
    if (a == null) {
      return notFound().build();
    }

    if (ts.getId() != null) {
      return badRequest().body("Don't pass in ids for creates");
    }
    if (ts.getGeotag() != null) {
      return badRequest().body("Don't pass geotag for creates");
    }
    if (!ts.getCreator().equals(a.getAssignee())) {
      return badRequest().body("Creator ODLCUser does not match ODLCUser of assignment");
    }
    ts.setAssignment(a);

    boolean geotagChanged = Geotag.attemptSetGeotagForTargetSighting(ts);
    getTargetSightingDao().create(ts);

    if (geotagChanged && ts.getTarget() != null) {
      // Updates target in dao
      Geotag.updateGeotag(ts.getTarget(), ts);

      if (cuairInteropRequests) {
        // TODO: Add back in once client code is complete
        // interopClient.updateTarget(ts.getTarget());
      }
    }
    return ok(ts);
  }

  /**
   * Constructs an HTTP response after updating a target sighting given an id
   *
   * @param id Long id of target sighting
   * @return the updated target sighting
   */
  public abstract ResponseEntity update(Long id, T other);

  /**
   * Constructs an HTTP response after updating a target sighting given an updated target sighting
   *
   * @param ts    Target sighting to be updated
   * @param other Target sighting with updated fields
   * @return the updated target sighting on success, 400 if supplied target sighting with updated
   * fields is incorrectly formatted
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

    if (other.getpixelx() != null && !other.getpixelx().equals(ts.getpixelx())) {
      return badRequest().body("Don't change value of pixel_x. Current value is " + ts.getpixelx());
    }
    if (other.getpixely() != null && !other.getpixely().equals(ts.getpixely())) {
      return badRequest().body("Don't change value of pixel_y. Current value is " + ts.getpixely());
    }
    if (other.getWidth() != null && !other.getWidth().equals(ts.getWidth())) {
      return badRequest().body("Don't change value of width. Current value is " + ts.getWidth());
    }
    if (other.getHeight() != null && !other.getHeight().equals(ts.getHeight())) {
      return badRequest().body("Don't change value of height. Current value is " + ts.getHeight());
    }
    ts.updateFromTargetSighting(other);

    getTargetSightingDao().update(ts);

    if (Geotag.attemptSetGeotagForTargetSighting(ts) && other.getTarget() != null) {
      Geotag.updateGeotag(ts.getTarget(), null);
      if (cuairInteropRequests) {
        // TODO: Add back in once client code is complete
        // interopClient.updateTarget(ts.getTarget());
      }
    }
    return ok(ts);
  }

  /**
   * Constructs an HTTP response after deleting a TargetSighting
   *
   * @param ts Target sighting
   * @return the deleted target sighting
   */
  public ResponseEntity delete(T ts) {
    getTargetSightingDao().delete(ts.getId());

    if (ts.getTarget() != null) {
      Geotag.updateGeotag(ts.getTarget(), null);
      if (cuairInteropRequests) {
        // TODO: Add back in once client code is complete
        // interopClient.updateTarget(ts.getTarget());
      }
    }
    return ok(ts);
  }
}
