package org.cuair.ground.controllers.target;

import java.util.LinkedList;
import java.util.List;
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
    // TODO: Change this route on the frontend
    @RequestMapping(value = "/assignment/{id}", method = RequestMethod.POST)
    public ResponseEntity create(@PathVariable Long id, @RequestBody HttpEntity<String> httpEntity) {
        String jsonString = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = null;
        try {
            json = mapper.readTree(jsonString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
        }

        AlphanumTargetSighting ts = null;
        try {
            ts = mapper.treeToValue(json, AlphanumTargetSighting.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when convert json to AlphanumTargetSighting instance: \n" + e);
        }

        if (ts.isOffaxis() != null && ts.isOffaxis()) {
            if (ts.getTarget() != null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't pass targets for off-axis sighting creates");
            ts.setTarget(alphaTargetDao.getOffaxisTarget());
        }

        final ResponseEntity retval = super.create(id, ts);

        if (retval.getStatusCodeValue() == 200) {
            if (ts.getCreator() == ClientType.ADLC) {
                // TODO: Implement client code
                // autopilotClient.sendAdlcRoi(alphaDao.getTopAdlcLocations(5));

                // Sets new target thumbnail
                if (ts.getTarget() != null) {
                    AlphanumTarget t = alphaTargetDao.get(ts.getTarget().getId());
                    t.setThumbnailTSId(ts.getId());
                    alphaTargetDao.update(t);
                    // TODO: Implement flags (used to be PlayConfig.CUAIR_INTEROP_REQUESTS)
                    if (false) {
                        // interopClient.updateTargetImage(ts);
                    }
                }
            }
        }
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
    public ResponseEntity update(@PathVariable Long id, @RequestBody HttpEntity<String> httpEntity) {
        AlphanumTargetSighting ts = alphaDao.get(id);
        if (ts == null) {
            return ResponseEntity.noContent().build();
        }

        String jsonString = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = null;
        try {
            json = mapper.readTree(jsonString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
        }

        AlphanumTargetSighting other = null;
        try {
            other = mapper.treeToValue(json, AlphanumTargetSighting.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when convert json to AlphanumTargetSighting instance: \n" + e);
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
                && ts.getTarget().getThumbnailTSId().equals(ts.getId());
        AlphanumTarget tToEraseFrom = ts.getTarget();
        // checks whether new thumb for new target should be updated
        boolean updateNewThumb =
            other.getTarget() != null
                && (ts.getTarget() == null
                    || !ts.getTarget().getId().equals(other.getTarget().getId()));

        final ResponseEntity retval = updateFromTargetSighting(ts, other);

        if (retval.getStatusCodeValue() == 200) {
            if (toEraseThumb) {
                if (ts.getCreator() == ClientType.MDLC) {
                    // If MDLC, set thumb for original target to default value
                    tToEraseFrom.setThumbnailTSId(0L);
                    // TODO (mariasam1): delete thumbnail through interop
                } else {
                    // If ADLC, set thumb for original target to most recent ts (or default if none)
                    AlphanumTargetSighting newThumb = alphaDao.getLastSightingForTarget(tToEraseFrom.getId());
                    if (newThumb != null) {
                        newThumb.setTarget(tToEraseFrom);
                        tToEraseFrom.setThumbnailTSId(newThumb.getId());
                        // TODO: Implement flags (used to be PlayConfig.CUAIR_INTEROP_REQUESTS)
                        if (false) {
                            // interopClient.updateTargetImage(newThumb);
                        }
                    } else {
                        tToEraseFrom.setThumbnailTSId(0L);
                        // TODO (mariasam1): delete thumbnail through interop
                    }
                }
                alphaTargetDao.update(tToEraseFrom);
            }

            if (ts.getCreator() == ClientType.ADLC) {
                // TODO: Implement client code
                // autopilotClient.sendAdlcRoi(alphaDao.getTopAdlcLocations(5));

                // Sets new thumbnail for updated target
                if (updateNewThumb) {
                    AlphanumTarget t = alphaTargetDao.get(ts.getTarget().getId());
                    t.setThumbnailTSId(ts.getId());
                    alphaTargetDao.update(t);
                    // TODO: Implement flags (used to be PlayConfig.CUAIR_INTEROP_REQUESTS)
                    if (false) {
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

        if (ts.getTarget() != null && ts.getTarget().getThumbnailTSId().equals(id)) {
            if (ts.getCreator() == ClientType.MDLC) {
                // If MDLC, set thumb for original target to default value
                ts.getTarget().setThumbnailTSId(0L);
                // TODO (mariasam1): delete thumbnail through interop
            } else {
                // If ADLC, set thumb for original target to most recent ts (or default if none)
                AlphanumTargetSighting newThumb = alphaDao.getLastSightingForTarget(ts.getTarget().getId());
                if (newThumb != null) {
                    ts.getTarget().setThumbnailTSId(newThumb.getId());
                    // TODO: Implement flags (used to be PlayConfig.CUAIR_INTEROP_REQUESTS)
                    if (false) {
                        // interopClient.updateTargetImage(newThumb);
                    }
                } else {
                    ts.getTarget().setThumbnailTSId(0L);
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
