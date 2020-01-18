package org.cuair.ground.controllers.target;

import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.plane.target.EmergentTarget;

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

/** Controller to handle creation/retrieval of Emergent Target model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/emergent_target")
public class EmergentTargetController extends TargetController<EmergentTarget> {

    /** Database accessor object for emergent targets */
    private static final ClientCreatableDatabaseAccessor<EmergentTarget> targetDao =
        (ClientCreatableDatabaseAccessor<EmergentTarget>) DAOFactory.getDAO(
            DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget.class);

    /** An object mapper */
    private ObjectMapper mapper = new ObjectMapper();

    /** Gets the database accessor object for this target */
    @Override
    public ClientCreatableDatabaseAccessor<EmergentTarget> getTargetDao() {
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
    // @ValidateJson(EmergentTarget::class)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody EmergentTarget t) {
        return super.create(t);
    }

    /**
     * Update Target by id
     *
     * @param id Long id of the Target being updated
     * @return the updated Target as JSON
     */
    // @ValidateJson(EmergentTarget::class)
    // TODO: Figure out if this is necessary
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity update(@PathVariable Long id, @RequestBody EmergentTarget other) {
        EmergentTarget t = targetDao.get(id);
        if (t == null) {
            return ResponseEntity.noContent().build();
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
