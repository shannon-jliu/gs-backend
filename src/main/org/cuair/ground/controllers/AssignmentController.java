package org.cuair.ground.controllers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.TimestampDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.AuthToken;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;
import org.cuair.ground.models.plane.target.EmergentTargetSighting;
import org.cuair.ground.models.plane.target.TargetSighting;
import org.cuair.ground.util.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.web.multipart.support.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/** API callbacks to handle creation/retrieval of Assignment model objects */
@RestController
@RequestMapping(value = "/auth")
public class AssignmentController {

    /** The database access object for the assignment database */
    private static final AssignmentDatabaseAccessor assignmentDao =
        (AssignmentDatabaseAccessor)
            DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);

    /** Database accessor object for image database */
    private static final TimestampDatabaseAccessor<Image> imageDao =
        (TimestampDatabaseAccessor<Image>)
            DAOFactory.getDAO(DAOFactory.ModelDAOType.TIMESTAMP_DATABASE_ACCESSOR, Image.class);

    /**
     * Gets all assignments
     *
     * @return Result containing all assignments as json
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Assignment>> getAll() {
        return ResponseEntity.ok(assignmentDao.getAll());
    }

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
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(a);
    }

    /**
     * Gets all assignments assigned to a user given their auth token. Should only be called when auth
     * is enabled
     *
     * @return Result containing all the user assignments as json
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ResponseEntity getByUser(@RequestParam String jsonString) {
        if (AUTH_ENABLED) {
            // grab user name
            ObjectNode json = null;
            try {
                json = (ObjectNode) mapper.readTree(jsonString);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
            }

            AuthToken token = AuthUtil.Companion.getToken(json);
            if (token != null) {
                return ResponseEntity.ok(assignmentDao.getAllForUser(token.getUsername()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username!");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Auth is disabled; no usernames");
        }
    }

    // TODO: Implement flags
    /** The flag to behave as if auth is enabled */
    // TODO: Figure out "final": It was taken away to be able to change the value of the field for testing
    private static Boolean AUTH_ENABLED = false;

    /** A logger */
    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    /** Default username if auth disabled */
    private static String DEFAULT_USER = "High Ground";

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
    public ResponseEntity createWork(@RequestParam String type, @RequestParam String jsonString) {
        ClientType assignee = ClientType.valueOf(type);
        Assignment a;

        // if auth disabled, continue with old behavior
        if (AUTH_ENABLED) {
            // grab user name and assign assignment to that username
            ObjectNode json = null;
            try {
                json = (ObjectNode) mapper.readTree(jsonString);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
            }

            AuthToken token = AuthUtil.Companion.getToken(json);
            if (token != null) {
                a = assignmentDao.getWork(assignee, token.getUsername());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username!");
            }
        } else {
            a = assignmentDao.getWork(assignee, DEFAULT_USER);
        }

        if (a == null) return ResponseEntity.noContent().build();

        assignmentDao.create(a);
        return ResponseEntity.ok(a);
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
    public ResponseEntity update(@RequestParam Long id, @RequestParam String jsonString) {
        Assignment a = assignmentDao.get(id);
        if (a == null) {
            return ResponseEntity.noContent().build();
        }
        if (jsonString != null) {
            ObjectNode json = null;
            try {
                json = (ObjectNode) mapper.readTree(jsonString);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
            }

            Assignment deserialized = null;
            try {
                deserialized = mapper.treeToValue(json, Assignment.class);
            } catch (JsonProcessingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when convert json to Assignment instance: \n" + e);
            }

            if (!a.getId().equals(deserialized.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Id in URL doesn't match id in object body");
            }
        }
        a.setDone(true);
        assignmentDao.update(a);
        return ResponseEntity.ok(a);
    }
}
