package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import javax.servlet.http.HttpServletRequest;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.UsernameDatabaseAccessor;
import org.cuair.ground.models.Username;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Contains all the callbacks for all the public api endpoints for the User  */
@CrossOrigin
@RestController
@RequestMapping(value = "/username")
public class UsernameController {
  private UsernameDatabaseAccessor usernameDao = (UsernameDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModellessDAOType.USERNAME_DATABASE_ACCESSOR);

  /**
   *
   * @param username - the username to create, sent as a raw object
   * @param request - the request object that will contain the remoteAddr of this request
   * @return 200 if the username is created successfully, otherwise 400 with a descriptive error message
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ResponseEntity create(@RequestBody String username, HttpServletRequest request) {
    String address = request.getRemoteAddr();
    String otherAddr = usernameDao.findAddrForUsername(username);

    if (otherAddr != null) {
      if (!otherAddr.equals(address)) {
        return badRequest().body("Username already exists: " + username);
      } else {
        // if it reaches here, then the username, address matches and the user is re-logging in
        return ok(username);
      }
    }

    String otherName = usernameDao.findUserForAddress(address);
    if (otherName != null) {
      return badRequest().body("Address already exists with username: " + otherName);
    }

    usernameDao.create(new Username(username, address));
    return ok(username);
  }
}
