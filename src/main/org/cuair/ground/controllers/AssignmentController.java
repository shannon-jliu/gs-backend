package org.cuair.ground.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.UsernameDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.util.Flags;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.*;

/** API callbacks to handle creation/retrieval of Assignment model objects. */
@CrossOrigin
@RestController
@RequestMapping(value = "/assignment")
public class AssignmentController {

  /** The database access object for the assignment database. */
  private AssignmentDatabaseAccessor assignmentDao =
      (AssignmentDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);

  /** The database access object for the username database. */
  private UsernameDatabaseAccessor usernameDao =
      (UsernameDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.USERNAME_DATABASE_ACCESSOR);

  /** The flag to behave as if auth is enabled. */
  private boolean AUTH_ENABLED = Flags.AUTH_ENABLED;

  /** Default username if auth disabled. */
  private String DEFAULT_USER = Flags.DEFAULT_USER;

  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Gets an assignment given an id
   *
   * @param id the id of the assignment to fetch
   * @return Result containing the assignment as json
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity<Assignment> get(@PathVariable Long id) {
    Assignment a = assignmentDao.get(id);
    if (a == null) {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ok(a);
  }

  /**
   * Gets all assignments whose id is greater than the given id.
   *
   * @param id the id to find all assignments strictly greater than
   * @return Result containing all relevant assignments as json
   */
  @RequestMapping(value = "/after/{id}", method = RequestMethod.GET)
  public ResponseEntity getAfterId(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
    // TODO: Implement auth once auth has been finalized
    return ok(assignmentDao.getAllAfterId(id, DEFAULT_USER));
  }

  /**
   * Generates an assignment for an unprocessed image and the given client type if auth is disabled
   *
   * <p>If clientType is MDLC and Auth is enabled, it will get the next image that the user is not
   * already assigned to
   *
   * @param type the type of client to assign the image to
   * @return Result containing the generated assignment as json
   */
  @RequestMapping(value = "/work/{type}", method = RequestMethod.POST)
  public ResponseEntity createWork(@RequestHeader HttpHeaders headers, @PathVariable String type) {
    ClientType assignee = ClientType.valueOf(type);
    Assignment a;

    if (AUTH_ENABLED) {
      // Set user to value sent in header
      String user = headers.getFirst("X-AUTH-TOKEN");
      // If username not in database, bad request
      if (usernameDao.findAddrForUsername(user) != null) {
        return badRequest().build();
      }
      a = assignmentDao.getWork(assignee, user);
    } else {
      a = assignmentDao.getWork(assignee, DEFAULT_USER);
    }

    if (a == null) {
      return noContent().build();
    }

    return ok(a);
  }

  /**
   * Updates the status of an assignment, marking it as done
   *
   * @param id the id of the assignment to update
   * @return Result containing the updated assignment as json
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResponseEntity update(@PathVariable Long id, @RequestBody Assignment deserialized) {
    Assignment a = assignmentDao.get(id);
    if (a == null) {
      return noContent().build();
    }
    if (deserialized != null) {
      if (!a.getId().equals(deserialized.getId())) {
        return badRequest().body("Id in URL doesn't match id in object body");
      }
    }
    a.setDone(true);
    assignmentDao.update(a);
    return ok(a);
  }
}
