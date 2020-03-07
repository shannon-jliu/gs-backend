package org.cuair.ground.controllers.target

import org.cuair.ground.clients.ClientFactory
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor
import org.cuair.ground.daos.DAOFactory
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor
import org.cuair.ground.models.ClientType
import org.cuair.ground.models.plane.target.Target
import org.cuair.ground.models.plane.target.TargetSighting
import org.cuair.ground.util.Flags;

import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import kotlin.concurrent.thread

/** Controller to handle creation/retrieval of Emergent Target model objects  */
abstract class TargetController<T : Target> {

    /** Gets the database accessor object for this target  */

    abstract fun getTargetDao(): ClientCreatableDatabaseAccessor<T>

    /**
     * Constructs an HTTP response with all the targets
     *
     * @return HTTP response with json of all the targets
     */
    open fun getAll(): ResponseEntity<Any> = ok(getTargetDao().all)

    /**
     * Get Target by id
     *
     * @param id Long id of the desired target
     * @return the Target as JSON
     */
    open operator fun get(id: Long?): ResponseEntity<Any> {
        val t = getTargetDao().get(id) ?: return noContent().build()
        return ok(t)
    }

    /**
     * Create Target
     *
     * @param t T target to be created
     * @return the created Target as JSON
     */
    open fun create(t: T): ResponseEntity<Any> {
        if (t.id != null) return badRequest().body("Don't pass ids for creates")
        if (t.creator == null) return badRequest().body("Create request should have creator")
        getTargetDao().create(t)
        interopClient.attemptSend(getTargetDao().get(t.id));
        // TODO: Add interop code
        // if (CUAIR_INTEROP_REQUESTS) {
        //     interopClient.createTarget(getTargetDao().get(t.id))
        //     thread {
        //         Thread.sleep(PlayConfig.TARGETLOGGER_DELAY)
        //         while (getTargetDao().get(t.id).judgeTargetId == null) {
        //             logger.warn("${t.typeString} Target ${t.id} not sent to judges!")
        //             Thread.sleep(PlayConfig.TARGETLOGGER_DELAY)
        //         }
        //     }
        // }
        return ok(t)
    }

    /**
     * Update Target by id
     *
     * @param t T original target to be updated
     * @param other T target with new values
     * @return the updated Target as JSON
     */
    fun update(t: T, other: T): ResponseEntity<Any> {
        logger.info("update target controller");
        val targetSightingDao = DAOFactory.getDAO(
            DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
            t.fetchAssociatedTargetSightingClass()) as TargetSightingsDatabaseAccessor<out TargetSighting>

        if (other.id !== null) {
            return badRequest().body("Don't pass ids for updates")
        }
        if (other.creator !== null) {
            return badRequest().body("Don't pass creator for updates")
        }

        val isTSIdUpdated = t.getthumbnail_tsid() != other.getthumbnail_tsid()
        logger.info("isTSIdUpdated? " + isTSIdUpdated);

        t.updateFromTarget(other)
        logger.info("update from target other done?");
        if (other.geotag !== null) {
            logger.info("other geotag is not null");
            targetSightingDao
                .getAllTargetSightingsForTarget(t.id)
                .forEach { it.geotag?.isManualGeotag = true }
        }
        logger.info("other geotag is not null after");
        getTargetDao().update(t)
        logger.info("other geotag is not null after update");
        if (isTSIdUpdated && CUAIR_INTEROP_REQUESTS) {
            logger.info("before update target image geotag is not null");
            interopClient.updateTargetImage(targetSightingDao.get(t.getthumbnail_tsid()))
            logger.info("after update target image");
        }
        //interopClient.updateTarget(getTargetDao().get(t.id))

        // TODO: Add client code
        if (CUAIR_INTEROP_REQUESTS) {
            logger.info("cuair interop requests before ");
            if (isTSIdUpdated) {
                logger.info("isTSIdUpdated inside");
                interopClient.updateTargetImage(targetSightingDao.get(t.getthumbnail_tsid()))
                logger.info("update target image");
            }
            logger.info("before attempt update");
            interopClient.attemptUpdate(getTargetDao().get(t.id))
            logger.info("after attempt update geotag is not null");
        }
        return ok(t)
    }

    /**
     * Deletes a Target and unassigns all TargetSightings that were assigned to this Target You must
     * send an empty body to do a delete.
     *
     * @param id Long id of target to be deleted
     * @return the deleted Target as JSON
     */
    open fun delete(id: Long?): ResponseEntity<Any> {
        val t = getTargetDao().get(id)

        if (t === null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val targetSightingDao = DAOFactory.getDAO(
            DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
            t.fetchAssociatedTargetSightingClass()) as TargetSightingsDatabaseAccessor<out TargetSighting>

        // TODO: Add client code
        if (CUAIR_INTEROP_REQUESTS) interopClient.attemptDelete(t)

        targetSightingDao.unassociateAllTargetSightingsForTarget(id)
        getTargetDao().delete(id)
        t.setGeotag(null);
        return ok(t);
    }

    companion object {
        /** The interop client for communication with the competition server */
        private val interopClient = ClientFactory.getInteropClient()
        /** A logger */
        private val logger = LoggerFactory.getLogger(TargetController::class.java)
        private val CUAIR_INTEROP_REQUESTS = Flags.CUAIR_INTEROP_REQUESTS;
    }
}
