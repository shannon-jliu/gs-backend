package org.cuair.ground.controllers.target;

import java.util.List;

// TODO: Add back in once client code is complete
// import org.cuair.ground.clients.ClientFactory;
// import org.cuair.ground.clients.InteropClient;

import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ODLCUserDatabaseAccessor;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.models.plane.target.TargetSighting;
import org.cuair.ground.util.Flags;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

  /** Gets name of type for error messages (e.g. "alphanum" or "emergent") */
  protected abstract String getTypeName();

  /**
   * Returns all target sightings
   *
   * @return a list of all target sightings in the db
   */
  public List<T> getAll() {
    return getTargetSightingDao().getAll();
  }

  /**
   * Returns new ts after updating a target sighting given an id
   *
   * @param id Long id of target sighting
   * @return Updated target sighting
   * @throws ResponseStatusException if new target sighting is invalid
   */
  public abstract T update(Long id, T other) throws ResponseStatusException;

  /**
   * Creates a target sighting in this assigned image
   *
   * @param assignmentId the id of the assignment for which to create target sighting
   * @return the created target sighting on success, 204 when the associated assignment
   * does not exist, or 400 when the request includes an id or geotag field or if the
   * creator field does not match the creator of the associated assignment
   * @throws ResponseStatusException if new target sighting is invalid
   */
  public T create(Long assignmentId, T ts) throws ResponseStatusException {
    Assignment a = assignmentDao.get(assignmentId);
    if (a == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          String.format("Assignment with id %d not found", assignmentId));
    }

    if (ts.getId() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't pass in ids for creates");
    }
    if (ts.getGeotag() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't pass geotag for creates");
    }
    // full objects cannot be compared here because of issue #61
    if (!ts.getCreator().getId().equals(a.getAssignee().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator ODLCUser does not match ODLCUser of assignment");
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

    return ts;
  }

  /**
   * Updates a target sighting given an updated target sighting
   *
   * @param ts    Target sighting to be updated
   * @param other Target sighting with updated fields
   * @return the updated target sighting on success, 400 if supplied target sighting with updated
   *    * fields is incorrectly formatted
   * @throws ResponseStatusException if new target sighting is invalid
   */
  T updateFromTargetSighting(T ts, T other) {
    if (other.getId() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't pass ids for updates");
    }
    if (other.getCreator() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't pass creator for updates");
    }
    if (other.getGeotag() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't pass geotag for updates");
    }

    if (other.getPixelx() != null && !other.getPixelx().equals(ts.getPixelx())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't change value of pixel_x. Current value is " + ts.getPixelx());
    }
    if (other.getPixely() != null && !other.getPixely().equals(ts.getPixely())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't change value of pixel_y. Current value is " + ts.getPixely());
    }
    if (other.getWidth() != null && !other.getWidth().equals(ts.getWidth())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't change value of width. Current value is " + ts.getWidth());
    }
    if (other.getHeight() != null && !other.getHeight().equals(ts.getHeight())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't change value of height. Current value is " + ts.getHeight());
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

    return ts;
  }

  /**
   * Deletes target sighting with id {@code id}
   *
   * @param id Long id of target sighting
   * @throws ResponseStatusException if new target sighting is invalid
   */
  public abstract void delete(Long id) throws ResponseStatusException;

  /**
   * Deletes a target sighting. Target sighting passed in must be the exact version saved on the ground server.
   *
   * @param ts target sighting to be deleted
   * @throws ResponseStatusException
   */
  protected void deleteFromSighting(T ts) throws ResponseStatusException {
    getTargetSightingDao().delete(ts.getId());

    if (ts.getTarget() != null) {
      Geotag.updateGeotag(ts.getTarget(), null);
      if (cuairInteropRequests) {
        // TODO: Add back in once client code is complete
        // interopClient.updateTarget(ts.getTarget());
      }
    }
  }
}
