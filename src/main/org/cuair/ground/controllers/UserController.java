package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.ok;

import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.DatabaseAccessor;
import org.cuair.ground.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Contains all the callbacks for all the public api endpoints for the User  */
@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class UserController {
  private DatabaseAccessor userDao = (DatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, User.class);

  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ResponseEntity create(String username) {
    return ok(username);
  }
}
