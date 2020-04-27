package org.cuair.ground.daos;

import io.ebean.Ebean;
import java.util.List;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.util.Flags;

/** Database Accessor object for ODLCUser model */
public class ODLCUserDatabaseAccessor extends DatabaseAccessor<ODLCUser> {
  ODLCUserDatabaseAccessor() {
    super(ODLCUser.class);
  }

  /**
   * Find out if the ODLCUser already exists in the database
   *
   * @param username the username to find
   * @return the address this ODLCUser is associated with, or null if the ODLCUser does not exist
   */
  public String getAddressFromUsername(String username) {
    ODLCUser odlcUserObj = Ebean.find(ODLCUser.class).where().eq("username", username).findOne();
    if (odlcUserObj != null) {
      return odlcUserObj.getAddress();
    }
    return null;
  }

  /**
   * Finds if there is an ODLCUser associated with this address
   * NOTE: ip addresses are not unique to users if ENABLE_MULTIPLE_USERS_PER_IP is false!
   *
   * @param address - the address to search the database for
   * @return the ODLCUser associated with this address, null if this address does not exist
   */
  public ODLCUser getODLCUserFromAddress(String address) {
    return Ebean.find(ODLCUser.class).where().eq("address", address).findOne();
  }

  /**
   * Finds the ODLCUser object associated with username in the database
   *
   * @param username the username string to find
   * @return the ODLCUser object associated with username, if it doesn't exist then null.
   */
  public ODLCUser getODLCUserFromUsername(String username) {
    return Ebean.find(ODLCUser.class).where().eq("username", username).findOne();
  }

  /**
   * Checks the existence of a username string
   *
   * @param username the username string to check the existence of
   * @return true if the ODLCUser exists with given username, false otherwise
   */
  public boolean usernameExists(String username) {
    return this.getODLCUserFromUsername(username) != null;
  }

  /**
   * Finds the ODLCUser object representing the MDLC operator
   *
   * @return the ODLCUser object associated with the MDLC operator, if it doesn't exist then null
   */
  public ODLCUser getMDLCOperatorUser() {
    return Ebean.find(ODLCUser.class).where().eq("userType", ODLCUser.UserType.MDLCOPERATOR)
        .findOne();
  }

  /**
   * Finds the ODLCUser object representing the ADLC user
   *
   * @return the ODLCUser object associated with the ADLC user, if it doesn't exist then null
   */
  public ODLCUser getADLCUser() {
    return Ebean.find(ODLCUser.class).where().eq("userType", ODLCUser.UserType.ADLC).findOne();
  }

  /**
   * Finds the ODLCUser objects representing all users that tag (i.e. all MDLC)
   *
   * @return the ODLCUser objects associated with the an MDLC tagger, if none exist then null
   */

  public List<ODLCUser> getMDLCTaggers() {
    return Ebean.find(ODLCUser.class).where().or().eq("userType", ODLCUser.UserType.MDLCOPERATOR)
        .eq("userType", ODLCUser.UserType.MDLCTAGGER).endOr().findList();
  }

  /**
   * Finds the default ODLCUser object and returns it. If one does not exist, it is created.
   * Note: if Flags.USERS_ENABLED = true, then null will always be returned and no model is created
   *
   * @return the ODLCUser object associated with the default user or null if USERS_ENABLED = true
   */
  public ODLCUser getDefaltUser() {
    if (Flags.USERS_ENABLED) {
      return null;
    }
    ODLCUser user =
        Ebean.find(ODLCUser.class).where().eq("userType", ODLCUser.UserType.MDLCOPERATOR)
            .eq("username", Flags.DEFAULT_USERNAME).findOne();
    if (user == null) {
      user = new ODLCUser(Flags.DEFAULT_USERNAME, "localhost", ODLCUser.UserType.MDLCOPERATOR);
      this.create(user);
    }
    return user;
  }
}
