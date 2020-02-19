package org.cuair.ground.daos;

import io.ebean.Ebean;
import org.cuair.ground.models.Username;

/**
 * Database Accessor object for Username model
 */
public class UsernameDatabaseAccessor extends DatabaseAccessor<Username> {
  UsernameDatabaseAccessor() {
    super(Username.class);
  }

  /**
   * Find out if the username already exists in the database
   * @param username the username to find
   * @return the address this username is associated with, or null if the username does not exist
   */
  public String findAddrForUsername(String username) {
    Username userObj = Ebean.find(Username.class).where().eq("username", username).findOne();
    if (userObj != null) {
      return userObj.getAddress();
    }
    return null;
  }

  /**
   * Finds if there is a username associated with this address
   * @param address - the address to search the database for
   * @return the username associated with this address, null if this address does not exist
   */
  public String findUserForAddress(String address) {
    Username userObj = Ebean.find(Username.class).where().eq("address", address).findOne();
    if (userObj != null) {
      return userObj.getUsername();
    }
    return null;
  }
}