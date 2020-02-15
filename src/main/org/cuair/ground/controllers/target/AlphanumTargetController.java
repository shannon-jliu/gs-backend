package org.cuair.ground.controllers.target;

import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.plane.target.AlphanumTarget;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;

/** Controller to handle creation/retrieval of Alphanumeric Target model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/alphanum_target")
public class AlphanumTargetController extends TargetController<AlphanumTarget> {

    /** Database accessor object for alphanum targets */
    private static final AlphanumTargetDatabaseAccessor<AlphanumTarget> targetDao =
        (AlphanumTargetDatabaseAccessor<AlphanumTarget>) DAOFactory.getDAO(
            DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

    /** An object mapper */
    private ObjectMapper mapper = new ObjectMapper();

    /** Gets the database accessor object for this target */
    public AlphanumTargetDatabaseAccessor<AlphanumTarget> getTargetDao() {
        return targetDao;
    }

    /**
     * Constructs an HTTP response with all the targets
     *
     * @return HTTP response with json of all the targets
     */
    @Override
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getAll() {
        return super.getAll();
    }

    /**
     * Constructs an HTTP response with the offaxis target
     *
     * @return HTTP response with json of the offaxis target
     */
    @RequestMapping(value = "/offaxis", method = RequestMethod.GET)
    public ResponseEntity getOffaxis() {
        return ResponseEntity.ok(getTargetDao().getOffaxisTarget());
    }

    /**
     * Get Target by id
     *
     * @param id Long id of the desired target
     * @return the Target as JSON
     */
    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity get(@PathVariable Long id) {
        return super.get(id);
    }

    /**
     * Create Target
     *
     * @return the created Target as JSON
     */
    // TODO: Figure out if this is necessary
    // @ValidateJson(AlphanumTarget.class)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody AlphanumTarget t) {
        // TODO: Figure out why these off-axis-related lines of code are here
        if (t.isOffaxis() != null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Don't pass offaxis for target creates");
        }
        t.setOffaxis(false);
        return super.create(t);
    }

    /**
     * Update Target by id
     *
     * @param id Long id of the Target being updated
     * @return the updated Target as JSON
     */
    // TODO: Figure out if this is necessary
    // @ValidateJson(AlphanumTarget.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity update(@PathVariable Long id, @RequestBody AlphanumTarget other) {
        AlphanumTarget t = targetDao.get(id);
        if (t == null) {
            return ResponseEntity.noContent().build();
        }

        // TODO: Figure out why these off-axis-related lines of code are here
        if (other.isOffaxis() != null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Don't pass offaxis for target creates");
        }
        return super.update(id, t, other);
    }

    /**
     * Deletes a Target and unassigns all TargetSightings that were assigned to this Target You must
     * send an empty body to do a delete.
     *
     * @param id Long id of target to be deleted
     * @return the deleted Target as JSON
     */
    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable Long id) {
        return super.delete(id);
    }
}
