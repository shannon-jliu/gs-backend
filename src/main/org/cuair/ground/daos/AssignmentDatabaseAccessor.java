package org.cuair.ground.daos;

import io.ebean.Ebean;
import io.ebean.Expr;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import java.util.List;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Database Accessor Object that provides an interface for persisting assignments. */
public class AssignmentDatabaseAccessor extends TimestampDatabaseAccessor<Assignment> {

    /** The database access object for the image database */
    private static final TimestampDatabaseAccessor<Image> imageDao = new TimestampDatabaseAccessor<>(Image.class);

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
        String ultimateSql =
            "SELECT id FROM (SELECT i.id AS id, MAX(COALESCE"
                + "(a.timestamp, timestamp'1970-01-01 00:00:00')) AS assign_time, "
                + "BOOL_OR(COALESCE(a.done, FALSE)) AS done, a.assignee, a.username FROM "
                + "(SELECT * FROM assignment WHERE assignee="
                + clientType.getId()
                + ") a FULL OUTER JOIN Image i ON a.image_id="
                + "i.id GROUP BY i.id, a.assignee, a.username) AS JoinedAssignments WHERE "
                + "DONE=false ORDER BY assign_time LIMIT 1";
        return getAssignment(ultimateSql, clientType, null);
    }

    /**
     * Creates an assignment for the earliest image that still needs to be processed by the given
     * client type and username. This method is duplicated to avoid pains of editing the tests
     *
     * @param clientType the type of client that is requesting work
     * @username the username of the user this is to be assigned to
     * @return an assignment that assigns the unprocessed image to the given client type
     */
    public Assignment getWork(ClientType clientType, String username) {
        String ultimateSql =
            "SELECT id FROM (SELECT i.id AS id, MAX(COALESCE"
                + "(a.timestamp, timestamp'1970-01-01 00:00:00')) AS assign_time, "
                + "BOOL_OR(COALESCE(a.done, FALSE)) AS done, a.assignee, a.username FROM "
                + "(SELECT * FROM assignment WHERE assignee="
                + clientType.getId()
                + ") a FULL OUTER JOIN Image i ON a.image_id="
                + "i.id GROUP BY i.id, a.assignee, a.username) AS JoinedAssignments WHERE "
                + "id NOT IN (SELECT image_id FROM assignment WHERE username='"
                + username
                + "')"
                + " ORDER BY assign_time LIMIT 1";
        return getAssignment(ultimateSql, clientType, username);
    }

    /**
     * Creates an assignment
     *
     * @param querySql the SQL query
     * @param clientType the client type (MDLC, ADLC)
     * @param username the username this assignment is assigned to
     * @return an assignment, if there is a new one to assign to the given user/clienttype, null if
     *     none
     */
    public Assignment getAssignment(String querySql, ClientType clientType, String username) {
        SqlQuery query = Ebean.createSqlQuery(querySql);
        List<SqlRow> result = query.findList();
        logger.info("result size: " + result.size());
        if (result.size() > 0) {
            Image image = imageDao.get(result.get(0).getLong("id"));
            Assignment a = new Assignment(image, clientType, username);
            a.setTimestamp(new java.sql.Timestamp(new java.util.Date().getTime()));
            return a;
        }
        return null;
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
