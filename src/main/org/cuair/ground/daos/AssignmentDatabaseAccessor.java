package org.cuair.ground.daos;

import io.ebean.Ebean;
import java.util.List;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.ODLCUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Database Accessor Object that provides an interface for persisting assignments. */
public class AssignmentDatabaseAccessor extends TimestampDatabaseAccessor<Assignment> {

  /** The database access object for the image database */
  private static final ImageDatabaseAccessor imageDao =
      (ImageDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.IMAGE_DATABASE_ACCESSOR);

  private static final Logger logger = LoggerFactory.getLogger(AssignmentDatabaseAccessor.class);

  /** Constructs a database accessor object for the assignment class */
  AssignmentDatabaseAccessor() {
    super(Assignment.class);
  }

  /**
   * Creates an assignment for the earliest image that still needs to be processed by the given
   * ODLCUser and commits it to the database.
   *
   * @return an assignment that assigns the unprocessed image to the given ODLCUser
   * @user the ODLCUser this assignment is to be assigned to
   */
  public Assignment getWork(ODLCUser user) {
    String propertyName;
    if (user.getUserType().equals(ODLCUser.UserType.ADLC)) {
      propertyName = "hasAdlcAssignment";
    } else {
      propertyName = "hasMdlcAssignment";
    }
    List<Image> i_list = Ebean.find(Image.class)
        .where()
        .eq(propertyName, false)
        .orderBy()
        .asc("id")
        .findList();
    if (i_list.isEmpty()) {
      return null;
    }
    Image i = i_list.get(0);
    Assignment a = new Assignment(i, user);
    a.setTimestamp(new java.sql.Timestamp(new java.util.Date().getTime()));
    this.create(a);
    if (user.getUserType().equals(ODLCUser.UserType.ADLC)) {
      imageDao.setImageHasADLCAssignment(i);
    } else {
      imageDao.setImageHasMDLCAssignment(i);
    }

    return a;
  }

  /**
   * Gets all assignments for a particular user
   *
   * @param user the ODLCUser object corresponding to the user requesting assignments
   * @return a list of assignments that are assigned to the given user
   */
  public List<Assignment> getAllForUser(ODLCUser user) {
    return Ebean.find(Assignment.class)
        .where()
        .eq("assignee", user)
        .orderBy()
        .asc("id")
        .findList();
  }

  /**
   * Retrieves all instances of Assignments that have the same image. Returns an empty list if no
   * such assignments exist in the database.
   *
   * @return List<Assignment> of all instances that don't have a Target
   */
  public List<Assignment> getAllForImageId(Long imageId) {
    return Ebean.find(getModelClass()).where().eq("image_id", imageId).findList();
  }
}
