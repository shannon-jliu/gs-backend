package org.cuair.ground.daos;

import io.ebean.Ebean;
import io.ebean.Expr;
import java.util.List;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.Image;

import org.cuair.ground.util.Flags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Database Accessor Object that provides an interface for persisting assignments. */
public class AssignmentDatabaseAccessor extends TimestampDatabaseAccessor<Assignment> {

    /** The database access object for the image database */
    private static final ImageDatabaseAccessor imageDao =
            (ImageDatabaseAccessor)
                    DAOFactory.getDAO(DAOFactory.ModellessDAOType.IMAGE_DATABASE_ACCESSOR);

    /** A logger */
    private static final Logger logger = LoggerFactory.getLogger(AssignmentDatabaseAccessor.class);

    /** Constructs a database accessor object for the assignment class */
    AssignmentDatabaseAccessor() {
        super(Assignment.class);
    }

    /**
     * Creates an assignment for the earliest image that still needs to be processed by the given
     * client type
     *
     * @param clientType the type of client that is requesting work
     * @return an assignment that assigns the unprocessed image to the given client type
     */
    public Assignment getWork(ClientType clientType) {
        return getWork(clientType, Flags.DEFAULT_USER);
    }

    /**
     * Creates an assignment for the earliest image that still needs to be processed by the given
     * client type and username and commits it to the database.
     * This method is duplicated to avoid pains of editing the tests
     *
     * @param clientType the type of client that is requesting work
     * @username the username of the user this is to be assigned to
     * @return an assignment that assigns the unprocessed image to the given client type
     */
    public Assignment getWork(ClientType clientType, String username) {
        List<Image> i_list = Ebean.find(Image.class)
                .where()
                .eq("hasAssignment", false)
                .orderBy()
                .asc("id")
                .findList();
        if (i_list.isEmpty()) {
            return null;
        }
        Assignment a = new Assignment(i_list.get(0), clientType, username);
        a.setTimestamp(new java.sql.Timestamp(new java.util.Date().getTime()));
        this.create(a);
        imageDao.setImageHasAssignment(i_list.get(0));
        return a;
    }

    /**
     * Retrieves all instances of Assignments that have the same image. Returns an empty list if no
     * such assignments exist in the database.
     *
     * @return List<T> of all instances that don't have a Target
     */
    public List<Assignment> getAllForImageId(Long imageId) {
        return Ebean.find(getModelClass()).where().eq("image_id", imageId).findList();
    }

    /**
     * Retrieves all instances of Assignments that are assigned to a user. Returns empty list if the
     * user either does not exist or has no assignments
     *
     * @param user a String containing the username
     * @return List<T> of all instances that are assigned to the user
     */
    public List<Assignment> getAllForUser(String user) {
        return Ebean.find(getModelClass()).where().eq("username", user).findList();
    }

    /**
     * Retrieves all assignment ids that are greater than the specified id. Only to be used when auth
     * is disabled. This will be MDLC only.
     *
     * @param id the id to find all assignments greater than
     * @param user the username to search for
     * @return a list of assignments whose ids are greater than the given id. Empty list if there are
     *     none.
     */
    public List<Assignment> getAllAfterId(Long id, String user) {
        return Ebean.find(getModelClass())
                .where(Expr.and(Expr.eq("username", user), Expr.gt("id", id)))
                .findList();
    }
}
