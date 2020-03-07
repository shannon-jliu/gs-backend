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

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.badRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/** Controller to handle creation/retrieval of Alphanumeric Target model objects */
@CrossOrigin
@RestController
@RequestMapping(value = "/alphanum_target")
public class AlphanumTargetController extends TargetController<AlphanumTarget> {

    /** Database accessor object for alphanum targets */
    private static final AlphanumTargetDatabaseAccessor<AlphanumTarget> targetDao =
        (AlphanumTargetDatabaseAccessor<AlphanumTarget>) DAOFactory.getDAO(
            DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

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
     * Create Target
     *
     * @return the created Target as JSON
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody AlphanumTarget t) {
        return super.create(t);
    }

    /**
     * Update Target by id
     *
     * @param id Long id of the Target being updated
     * @return the updated Target as JSON
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity update(@PathVariable Long id, @RequestBody AlphanumTarget other) {
        System.out.println("alphanum update");
        AlphanumTarget t = targetDao.get(id);
        if (t == null) {
            System.out.println("t is null alphanum update return not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        System.out.println("alphanum update KEK");
        return super.update(t, other);
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
