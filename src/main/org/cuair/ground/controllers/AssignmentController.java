package org.cuair.ground.controllers;

import java.io.IOException;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.AuthToken;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.util.AuthUtil;
import org.cuair.ground.util.Flags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.beans.factory.annotation.Value;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.badRequest;

/** API callbacks to handle creation/retrieval of Assignment model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/assignment")
public class AssignmentController {

    /** The database access object for the assignment database */
    private static final AssignmentDatabaseAccessor assignmentDao =
        (AssignmentDatabaseAccessor)
            DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);

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
            ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ok(a);
    }

    /**
     * Gets all assignments whose id is greater than the given id.
     *
     * @param id the id to find all assignments strictly greater than
     * @return Result containing all relevant assignments as json
     */
    @RequestMapping(value = "/after/{id}", method = RequestMethod.GET)
    public ResponseEntity getAfterId(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        if (AUTH_ENABLED) {
            // grab user name
            AuthToken token = AuthUtil.Companion.getToken(headers);
            if (token != null) {
                return ok(assignmentDao.getAllAfterId(id, token.getUsername()));
            } else {
                return badRequest().body("Invalid username!");
            }
        } else {
            return ok(assignmentDao.getAllAfterId(id, DEFAULT_USER));
        }
    }

    /** The flag to behave as if auth is enabled */
    private boolean AUTH_ENABLED = Flags.AUTH_ENABLED;

    /** Default username if auth disabled */
    private String DEFAULT_USER = Flags.DEFAULT_USER;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Generates an assignment for an unprocessed image and the given client type if auth is disabled
     *
     * <p>If clientType is MDLC and Auth is enabled, it will get the next image that the user is not
     * already assigned to
     *
     * @param clientType the type of client to assign the image to
     * @return Result containing the generated assignment as json
     */
    @RequestMapping(value = "/work/{type}", method = RequestMethod.POST)
    public ResponseEntity createWork(@RequestHeader HttpHeaders headers, @PathVariable String type) {
        ClientType assignee = ClientType.valueOf(type);
        Assignment a;

        // if auth disabled, continue with old behavior
        if (AUTH_ENABLED) {
            // grab user name and assign assignment to that username
            AuthToken token = AuthUtil.Companion.getToken(headers);
            if (token != null) {
                a = assignmentDao.getWork(assignee, token.getUsername());
            } else {
                return badRequest().body("Invalid username!");
            }
        } else {
            a = assignmentDao.getWork(assignee, DEFAULT_USER);
        }

        if (a == null) return noContent().build();

        assignmentDao.create(a);
        return ok(a);
    }

    /**
     * Updates the status of an assignment, marking it as done
     *
     * @param id the id of the assignment to update
     * @return Result containing the updated assignment as json
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    // TODO: Is this necessary?
    // @ValidateJson(Assignment.class)
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
}
