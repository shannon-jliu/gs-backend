package org.cuair.ground.controllers.target;

import java.util.LinkedList;
import java.util.List;
import org.cuair.ground.clients.ClientFactory;
import org.cuair.ground.clients.InteropClient;
import org.cuair.ground.daos.*;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ClientCreatable;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

import org.cuair.ground.util.Flags;

/** Controller to handle Alphanumeric Target sightings model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/alphanum_target_sighting")
public class AlphanumTargetSightingController extends TargetSightingController<AlphanumTargetSighting> {

    private static final AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting> alphaDao =
        (AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting>)
            DAOFactory.getDAO(
                DAOFactory.ModelDAOType.ALPHANUM_TARGET_SIGHTINGS_DATABASE_ACCESSOR,
                AlphanumTargetSighting.class);

    private static final AlphanumTargetDatabaseAccessor<AlphanumTarget> alphaTargetDao =
        (AlphanumTargetDatabaseAccessor<AlphanumTarget>)
            DAOFactory.getDAO(
                DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

    /** The database access object for the image table */
    private static final TimestampDatabaseAccessor<Image> imageDao =
        (TimestampDatabaseAccessor<Image>)
            DAOFactory.getDAO(DAOFactory.ModelDAOType.TIMESTAMP_DATABASE_ACCESSOR, Image.class);

    /** The database access object for the assignment table */
    private static final AssignmentDatabaseAccessor assignmentDao =
        (AssignmentDatabaseAccessor)
            DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);

    /** An object mapper */
    private static final ObjectMapper mapper = new ObjectMapper();

    private InteropClient interopClient = ClientFactory.getInteropClient();

    private static boolean CUAIR_INTEROP_REQUESTS = Flags.CUAIR_INTEROP_REQUESTS;

    /**
     * Gets the TargetSightingDatabaseAccessor object for this target sighting
     *
     * @return the target sighting database accessor object
     */
    @Override
    TargetSightingsDatabaseAccessor<AlphanumTargetSighting> getTargetSightingDao() {
        return alphaDao;
    }

    /**
     * Constructs an HTTP response with all the target sightings
     *
     * @return HTTP response with the json of all the target sightings
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getAll() {
        return super.getAll();
    }

    // TODO: Abstract out object to/from json conversion

    /**
     * Constructs an HTTP response with all target sightings that are associated with a given creator
     * type
     *
     * @return HTTP response with the json of all the target sightings associated with given creator
     *     type
     */
    @RequestMapping(value = "/creator/{type}", method = RequestMethod.GET)
    public ResponseEntity getAllForCreator(@PathVariable String type) {
        return super.getAllForCreator(type);
    }

    /** Changes all ADLC target sightings to MDLC */
    @RequestMapping(value = "/MDLC", method = RequestMethod.PUT)
    public ResponseEntity changeToMDLC() {
        List<AlphanumTargetSighting> ts = alphaDao.getAllForCreator(ClientType.ADLC);
        for (AlphanumTargetSighting t : ts) {
            t.getAssignment().setAssignee(ClientType.MDLC);
            t.setCreator(ClientType.MDLC);
            alphaDao.update(t);
        }

        List<AlphanumTarget> targets = alphaTargetDao.getAllForCreator(ClientType.ADLC);
        for (AlphanumTarget targ : targets) {
            targ.setCreator(ClientType.MDLC);
            alphaTargetDao.update(targ);
            // interopClient.updateTarget(targ);
        }

        List<ClientCreatable> targetsAndSightings = new LinkedList<>();
        targetsAndSightings.addAll(ts);
        targetsAndSightings.addAll(targets);
        return ResponseEntity.ok(targetsAndSightings);
    }

    /**
     * Creates an alphanumeric target sighting in this assigned image
     *
     * @param id the id of the assignment for which to create target sighting
     * @return ResponseEntity containing the newly created target sighting as json
     */
    // TODO: Figure out if this is necessary
    // @ValidateJson(AlphanumTargetSighting.class)
    @RequestMapping(value = "/assignment/{id}", method = RequestMethod.POST)
    public ResponseEntity create(@PathVariable Long id, @RequestBody AlphanumTargetSighting ts) {
        System.out.println("alpha create");
        if (ts.isOffaxis() != null && ts.isOffaxis()) {
            if (ts.getTarget() != null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't pass targets for off-axis sighting creates");
            ts.setTarget(alphaTargetDao.getOffaxisTarget());
        }
        System.out.println("alpha create before super");

        final ResponseEntity retval = super.create(id, ts);

        if (retval.getStatusCodeValue() == 200) {
            if (ts.getCreator() == ClientType.ADLC) {
                // TODO: Implement client code
                // autopilotClient.sendAdlcRoi(alphaDao.getTopAdlcLocations(5));

                // Sets new target thumbnail
                if (ts.getTarget() != null) {
                    AlphanumTarget t = alphaTargetDao.get(ts.getTarget().getId());
                    t.setthumbnail_tsid(ts.getId());
                    alphaTargetDao.update(t);
                    // TODO: Implement flags (used to be PlayConfig.CUAIR_INTEROP_REQUESTS)
                    if (CUAIR_INTEROP_REQUESTS) {
                        interopClient.updateTargetImage(ts);
                    }
                }
            }
        }
        System.out.println("about to return");
        return retval;
    }

    /**
     * Constructs an HTTP response after updating an AlphanumTargetSighting given an id
     *
     * @param id Long id of target sighting
     * @return HTTP response with json of updated target sighting
     */
    // TODO: Figure out if this is necessary
    // @ValidateJson(AlphanumTargetSighting.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity update(@PathVariable Long id, @RequestBody AlphanumTargetSighting other) {
        System.out.println("update alphanum target sighting controller");
        AlphanumTargetSighting ts = alphaDao.get(id);
        if (ts == null) {
            return ResponseEntity.noContent().build();
        }

        // checks if the target is offaxis or updated to offaxis and if a target is set
        if (((ts.isOffaxis() && other.isOffaxis() == null) || (Boolean.TRUE.equals(other.isOffaxis())))
                && other.getTarget() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't pass targets for off-axis sighting updates");
        }

        if (Boolean.TRUE.equals(other.isOffaxis())) {
            other.setTarget(alphaTargetDao.getOffaxisTarget());
        }

        // Saved for later because if target is ADLC, we want to wait for the ts to be erased from the
        // db
        // checks whether thumbnail for current target should be saved
        boolean toEraseThumb =
            ts.getTarget() != null
                && (other.getTarget() == null
                    || !ts.getTarget().getId().equals(other.getTarget().getId()))
                // TODO: Fix: This threw a NullPointerException when moving a target sighting from its correct target to another one
                // The console on the frontend also threw an error: "index.js:1437 Warning: Can't perform a React state update on an unmounted component. This is a no-op, but it indicates a memory leak in your application. To fix, cancel all subscriptions and asynchronous tasks in the componentWillUnmount method.
                // in MergeSightingPreview (at mergeTarget.js:314)""
                && ts.getTarget().getthumbnail_tsid() != null && (ts.getTarget().getthumbnail_tsid().equals(ts.getId()));
        AlphanumTarget tToEraseFrom = ts.getTarget();

        System.out.println("t to erase from" + tToEraseFrom);
        // checks whether new thumb for new target should be updated
        boolean updateNewThumb =
            other.getTarget() != null
                && (ts.getTarget() == null
                    || !ts.getTarget().getId().equals(other.getTarget().getId()));

        final ResponseEntity retval = updateFromTargetSighting(ts, other);

        if (retval.getStatusCodeValue() == 200) {
            System.out.println("to erase thumb " + toEraseThumb);
            if (toEraseThumb) {
                if (ts.getCreator() == ClientType.MDLC) {
                    // If MDLC, set thumb for original target to default value
                    tToEraseFrom.setthumbnail_tsid(0L);
                    System.out.println("set 0");
                    // TODO (mariasam1): delete thumbnail through interop
                } else {
                    // If ADLC, set thumb for original target to most recent ts (or default if none)
                    AlphanumTargetSighting newThumb = alphaDao.getLastSightingForTarget(tToEraseFrom.getId());
                    if (newThumb != null) {
                        newThumb.setTarget(tToEraseFrom);
                        tToEraseFrom.setthumbnail_tsid(newThumb.getId());
                        // TODO: Implement flags (used to be PlayConfig.CUAIR_INTEROP_REQUESTS)
                        if (CUAIR_INTEROP_REQUESTS) {
                            interopClient.updateTargetImage(newThumb);
                        }
                    } else {
                        tToEraseFrom.setthumbnail_tsid(0L);
                        // TODO (mariasam1): delete thumbnail through interop
                    }
                }
                System.out.println("udpate alpha dao");
                alphaTargetDao.update(tToEraseFrom);
            }

            if (ts.getCreator() == ClientType.ADLC) {
                // TODO: Implement client code
                // autopilotClient.sendAdlcRoi(alphaDao.getTopAdlcLocations(5));

                // Sets new thumbnail for updated target
                if (updateNewThumb) {
                    AlphanumTarget t = alphaTargetDao.get(ts.getTarget().getId());
                    t.setthumbnail_tsid(ts.getId());
                    alphaTargetDao.update(t);
                    // TODO: Implement flags (used to be PlayConfig.CUAIR_INTEROP_REQUESTS)
                    if (CUAIR_INTEROP_REQUESTS) {
                        // interopClient.updateTargetImage(ts);
                    }
                }
            }
        }
        return retval;
    }

    /**
     * Constructs an HTTP response after deleting a TargetSighting given an id
     *
     * @param id Long id of target sighting
     * @return HTTP response with json of deleted target sighting
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable Long id) {
        AlphanumTargetSighting ts = alphaDao.get(id);

        if (ts == null) {
            return ResponseEntity.noContent().build();
        }

        final ResponseEntity retval = super.delete(ts);

        if (ts.getTarget() != null && ts.getTarget().getthumbnail_tsid().equals(id)) {
            if (ts.getCreator() == ClientType.MDLC) {
                // If MDLC, set thumb for original target to default value
                ts.getTarget().setthumbnail_tsid(0L);
                // TODO (mariasam1): delete thumbnail through interop
            } else {
                // If ADLC, set thumb for original target to most recent ts (or default if none)
                AlphanumTargetSighting newThumb = alphaDao.getLastSightingForTarget(ts.getTarget().getId());
                if (newThumb != null) {
                    ts.getTarget().setthumbnail_tsid(newThumb.getId());
                    // TODO: Implement flags (used to be PlayConfig.CUAIR_INTEROP_REQUESTS)
                    if (CUAIR_INTEROP_REQUESTS) {
                        // interopClient.updateTargetImage(newThumb);
                    }
                } else {
                    ts.getTarget().setthumbnail_tsid(0L);
                }
            }
            alphaTargetDao.update(ts.getTarget());
        }
        // TODO: Implement client code
        // if (ts.getCreator() == ClientType.ADLC)
        //     autopilotClient.sendAdlcRoi(alphaDao.getTopAdlcLocations(5));
        return retval;
    }
}
