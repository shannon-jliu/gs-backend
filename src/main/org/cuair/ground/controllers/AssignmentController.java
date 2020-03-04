package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ImageDatabaseAccessor;
import org.cuair.ground.daos.ODLCUserDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.util.Flags;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** API callbacks to handle creation/retrieval of Assignment model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/assignment")
public class AssignmentController {

  /** The flag to behave as if auth is enabled */
  private static boolean USERS_ENABLED = Flags.USERS_ENABLED;

  /** The database accessor object for the assignment database */
  private AssignmentDatabaseAccessor assignmentDao =
      (AssignmentDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
  /**The database accessor object for the image database */
  private ImageDatabaseAccessor imageDao =
      (ImageDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.IMAGE_DATABASE_ACCESSOR);
  /** The database accessor object for the username database */
  private ODLCUserDatabaseAccessor odlcUserDao =
      (ODLCUserDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.ODLCUSER_DATABASE_ACCESSOR);

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
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ok(a);
  }

  /**
   * Generates an assignment for an unprocessed image and the given client if auth is disabled
   * If the client is MDLC and Auth is enabled, it will get the next image that the user is not
   * already assigned to
   *
   * @return Result containing the generated assignment as json
   */
  @RequestMapping(value = "/work", method = RequestMethod.POST)
  public ResponseEntity createWork(@RequestHeader HttpHeaders headers) {
    ODLCUser user;
    if (USERS_ENABLED) {
      user = this.extractUserFromHeaders(headers);
      if (user == null) {
        return badRequest().body("Provided username does not exist. Try logging in.");
      }
    } else {
      user = odlcUserDao.getDefaltUser();
    }
    Assignment a = assignmentDao.getWork(user);
    if (a == null) {
      return noContent().build();
    }
    return ok(a);
  }

  /**
   * Gets all assignments, whether completed or not, for a given user
   *
   * @return Result containing a list of assignments as json
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity getAllForUser(@RequestHeader HttpHeaders headers) {
    ODLCUser user;
    if (USERS_ENABLED) {
      user = this.extractUserFromHeaders(headers);
      if (user == null) {
        return badRequest().body("Provided username does not exist. Try logging in.");
      }
    } else {
      user = odlcUserDao.getDefaltUser();
    }
    List<Assignment> a_list = assignmentDao.getAllForUser(user);
    return ok(a_list);
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

  /**
   * Gets the username string from the header and returns the associated ODLCUser object
   *
   * @param headers the HttpHeaders object from the request
   * @return ODLCUser corresponding to the username in the headers or null if none exists
   */
  private ODLCUser extractUserFromHeaders(HttpHeaders headers) {
    String username = headers.getFirst("Username");
    return odlcUserDao.getODLCUserFromUsername(username);
  }
}
