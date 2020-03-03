package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import javax.servlet.http.HttpServletRequest;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ODLCUserDatabaseAccessor;
import org.cuair.ground.models.ODLCUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.cuair.ground.util.Flags;

/** Contains all the callbacks for all the public api endpoints for the ODLCUser  */
@CrossOrigin
@RestController
@RequestMapping(value = "/odlcuser")
public class ODLCUserController {
  private ODLCUserDatabaseAccessor odlcUserDao = (ODLCUserDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModellessDAOType.ODLCUSER_DATABASE_ACCESSOR);

  /**
   *
   * @param username - the username to create, sent as a raw object
   * @param request - the request object that will contain the remoteAddr of this request
   * @return 200 if the username is created successfully, otherwise 400 with a descriptive error message
   */
  @RequestMapping(value = "/create/mdlc", method = RequestMethod.GET)
  public ResponseEntity create(@RequestHeader("Username") String username, HttpServletRequest request) {
    String address = request.getRemoteAddr();
    String otherAddr = odlcUserDao.getAddressFromUsername(username);

    if (otherAddr != null) {
      if (!otherAddr.equals(address)) {
        return badRequest().body("Username already exists: " + username);
      } else {
        // if it reaches here, then the username, address matches and the user is re-logging in
        return ok(username);
      }
    }

    if (!Flags.ENABLE_MULTIPLE_USERS_PER_IP) {
      ODLCUser other = odlcUserDao.getODLCUserFromAddress(address);
      if (other != null) {
        return badRequest().body("User " + other.getUsername() + " already exists from this host: " + address);
      }
    }

    if (username.equals(Flags.MDLC_OPERATOR_USERNAME)) {
      odlcUserDao.create(new ODLCUser(username, address, ODLCUser.UserType.MDLCOPERATOR));
    } else {
      odlcUserDao.create(new ODLCUser(username, address, ODLCUser.UserType.MDLCTAGGER));
    }
    return ok(username);
  }

  @RequestMapping(value = "/create/adlc", method = RequestMethod.GET)
  public ResponseEntity create(HttpServletRequest request) {
    if (odlcUserDao.getADLCUser() != null) {
      return badRequest().body("ADLC user already exists!");
    }

    String address = request.getRemoteAddr();
    if (!Flags.ENABLE_MULTIPLE_USERS_PER_IP) {
      ODLCUser other = odlcUserDao.getODLCUserFromAddress(address);
      if (other != null) {
        return badRequest().body("User " + other.getUsername() + " already exists from this host: " + address);
      }
    }

    odlcUserDao.create(new ODLCUser("adlc", address, ODLCUser.UserType.ADLC));
    return ok("adlc");
  }
}
