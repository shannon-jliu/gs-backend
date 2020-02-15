package org.cuair.ground.controllers.target;

import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetDatabaseAccessor;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.plane.target.EmergentTarget;
import org.cuair.ground.models.plane.target.EmergentTargetSighting;

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

/** Controller to handle Emergent Target sightings model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/emergent_target_sighting")
public class EmergentTargetSightingController extends TargetSightingController<EmergentTargetSighting> {

    /** Database accessor object for emergent target sightings */
    private static final TargetSightingsDatabaseAccessor<EmergentTargetSighting> eSightingDao =
        (TargetSightingsDatabaseAccessor<EmergentTargetSighting>)
            DAOFactory.getDAO(
                DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
                EmergentTargetSighting.class);

    /** Database accessor object for emergent targets */
    private static final TargetDatabaseAccessor<EmergentTarget> eTargetDao =
        (TargetDatabaseAccessor<EmergentTarget>)
            DAOFactory.getDAO(DAOFactory.ModelDAOType.TARGET_DATABASE_ACCESSOR, EmergentTarget.class);

    /**
     * Returns the TargetSightingDatabaseAccessor for this target sighting
     *
     * @return
     */
    @Override
    TargetSightingsDatabaseAccessor<EmergentTargetSighting> getTargetSightingDao() {
        return eSightingDao;
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

    /**
     * Constructs an HTTP response with a TargetSighting given an id.
     *
     * @param id Long id of TargetSighting
     * @return HTTP response with json of desired target sighting
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity get(@PathVariable Long id) {
        return super.get(id);
    }

    /**
     * Creates a target sighting in this assigned image
     *
     * @param id the id of the assignment for which to create target sighting
     * @return Result containing the newly created target sighting as json
     */
    // TODO: Figure out if this is necessary
    // @ValidateJson(EmergentTargetSighting.class)
    @RequestMapping(value = "/assignment/{id}", method = RequestMethod.POST)
    public ResponseEntity create(@PathVariable Long id, @RequestBody EmergentTargetSighting ts) {
        EmergentTarget t = eTargetDao.getAll().get(0);
        if (ts.getCreator() != ClientType.MDLC) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only MDLC should be creating Emergent Target Sightings");
        }
        ts.setTarget(t);
        return super.create(id, ts);
    }

    /**
     * Constructs an HTTP response after updating an EmergentTargetSighting given an id
     *
     * @param id Long id of target sighting
     * @return HTTP response with json of updated target sighting
     */
    // TODO: Figure out if this is necessary
    // @ValidateJson(EmergentTargetSighting.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity update(@PathVariable Long id, @RequestBody EmergentTargetSighting other) {
        if (other.getTarget() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't pass targets for emergent target sighting update");
        }
        EmergentTargetSighting ts = eSightingDao.get(id);
        if (ts == null) {
            return ResponseEntity.noContent().build();
        }
        return updateFromTargetSighting(ts, other);
    }

    /**
     * Constructs an HTTP response after deleting a TargetSighting given an id
     *
     * @param id Long id of target sighting
     * @return HTTP response with json of deleted target sighting
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable Long id) {
        EmergentTargetSighting ts = eSightingDao.get(id);

        if (ts == null) {
            return ResponseEntity.noContent().build();
        }

        if (ts.getTarget() != null && ts.getTarget().getthumbnail_tsid() == id) {
            ts.getTarget().setthumbnail_tsid(0L);
            eTargetDao.update(ts.getTarget());
            // TODO (mariasam1): delete thumbnail through interop
        }

        return super.delete(ts);
    }
}
