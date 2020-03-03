package org.cuair.ground.controllers.target;

import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetDatabaseAccessor;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.plane.target.EmergentTarget;
import org.cuair.ground.models.plane.target.EmergentTargetSighting;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.badRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;

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
     * @return the target sighting database accessor object
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
     * Creates a target sighting in this assigned image
     *
     * @param id the id of the assignment for which to create target sighting
     * @return Result containing the newly created target sighting as json
     */
    @RequestMapping(value = "/assignment/{id}", method = RequestMethod.POST)
    public ResponseEntity create(@PathVariable Long id, @RequestBody EmergentTargetSighting ts) {
        // TODO: Fix: This threw a NullPointerException when moving a target sighting to a target. Neither were emergent though
        // The console on the frontend also threw an error: "index.js:1437 Warning: Can't perform a React state update on an unmounted component. This is a no-op, but it indicates a memory leak in your application. To fix, cancel all subscriptions and asynchronous tasks in the componentWillUnmount method.
        // in MergeSightingPreview (at mergeTarget.js:314)""
        EmergentTarget t = eTargetDao.getAll().get(0);
        if (ts.getCreator().getUserType() != ODLCUser.UserType.MDLCTAGGER && ts.getCreator().getUserType() != ODLCUser.UserType.MDLCOPERATOR) {
            return badRequest().body("Only MDLC should be creating Emergent Target Sightings");
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
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity update(@PathVariable Long id, @RequestBody EmergentTargetSighting other) {
        if (other.getTarget() != null) {
            return badRequest().body("Don't pass targets for emergent target sighting update");
        }
        EmergentTargetSighting ts = eSightingDao.get(id);
        if (ts == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (ts.getTarget() != null && ts.getTarget().getthumbnail_tsid() == id) {
            ts.getTarget().setthumbnail_tsid(0L);
            eTargetDao.update(ts.getTarget());
            // TODO (mariasam1): delete thumbnail through interop
        }

        return super.delete(ts);
    }
}
