package org.cuair.ground.controllers.target

// TODO: Add back in once client code is complete
// import org.cuair.ground.clients.ClientFactory
import org.cuair.ground.daos.DAOFactory
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor
import org.cuair.ground.models.plane.target.Target
import org.cuair.ground.models.plane.target.TargetSighting

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
        // TODO: Add interop code
        // if (CUAIR_INTEROP_REQUESTS) {
        //     interopClient.createTarget(getTargetDao().get(t.id))
        //     thread {
        //         Thread.sleep(TARGETLOGGER_DELAY)
        //         while (getTargetDao().get(t.id).judgeTargetId == null) {
        //             logger.warn("${t.typeString} Target ${t.id} not sent to judges!")
        //             Thread.sleep(TARGETLOGGER_DELAY)
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

        t.updateFromTarget(other)
        if (other.geotag !== null) {
            targetSightingDao
                .getAllTargetSightingsForTarget(t.id)
                .forEach { it.geotag?.isManualGeotag = true }
        }
        getTargetDao().update(t)

        // TODO: Add client code
        // if (CUAIR_INTEROP_REQUESTS) {
        //     if (isTSIdUpdated) {
        //         interopClient.updateTargetImage(targetSightingDao.get(t.thumbnail_tsid))
        //     }
        //     interopClient.updateTarget(getTargetDao().get(t.id))
        // }
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
        // if (CUAIR_INTEROP_REQUESTS) interopClient.deleteTarget(t)

        targetSightingDao.unassociateAllTargetSightingsForTarget(id)
        getTargetDao().delete(id)
        return ok(t)
    }

    companion object {
        // TODO: Add client code
        /** The interop client for communication with the competition server */
        // private val interopClient = ClientFactory.getInteropClient()

        /** A logger */
        private val logger = LoggerFactory.getLogger(TargetSighting::class.java)
    }
}
