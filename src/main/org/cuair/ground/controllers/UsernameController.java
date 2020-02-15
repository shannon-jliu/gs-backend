package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.ok;

import javax.servlet.http.HttpServletRequest;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.DatabaseAccessor;
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
  private DatabaseAccessor usernameDao = (DatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, Username.class);

  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ResponseEntity create(@RequestBody String username, HttpServletRequest request) {
    String address = request.getRemoteAddr();
    return ok(username);
  }
}
