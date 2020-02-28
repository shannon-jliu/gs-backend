package org.cuair.ground.controllers.target

import org.cuair.ground.clients.ClientFactory
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor
import org.cuair.ground.daos.DAOFactory
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor
import org.cuair.ground.models.ClientType
import org.cuair.ground.models.plane.target.Target
import org.cuair.ground.models.plane.target.TargetSighting
import org.cuair.ground.util.Flags;

import org.springframework.http.ResponseEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

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
    open fun getAll(): ResponseEntity<Any> = ResponseEntity.ok(getTargetDao().all)

    /**
     * Constructs an HTTP response with all target sightings associated with a given creator type
     *
     * @return HTTP response with the json of all target sightings associated with given creator type
     */
    open fun getAllForCreator(type: String): ResponseEntity<Any> {
        val creator = ClientType.valueOf(type)
        return ResponseEntity.ok(getTargetDao().getAllForCreator(creator))
    }

    /**
     * Constructs an HTTP response with the ids of all the targets
     *
     * @return HTTP response with ids of all the targets
     */
    open fun getAllIds(): ResponseEntity<Any> = ResponseEntity.ok(getTargetDao().allIds)

    /**
     * Get Target by id
     *
     * @param id Long id of the desired target
     * @return the Target as JSON
     */
    open operator fun get(id: Long?): ResponseEntity<Any> {
        val t = getTargetDao().get(id) ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(t)
    }

    /**
     * Gets all TargetSightings with the same target (given that target's id)
     *
     * @param id Long id of the Target
     * @return HTTP response
     */
    open fun getTargetSightings(id: Long?): ResponseEntity<Any> {
        val t = getTargetDao().get(id) ?: return ResponseEntity.noContent().build()
        val targetSightingDao = DAOFactory.getDAO(
            DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
            t.fetchAssociatedTargetSightingClass()) as TargetSightingsDatabaseAccessor<out TargetSighting>
        return ResponseEntity.ok(targetSightingDao.getAllTargetSightingsForTarget(id))
    }

    /**
     * Create Target
     *
     * @param t T target to be created
     * @return the created Target as JSON
     */
    open fun create(t: T): ResponseEntity<Any> {
        if (t.id != null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't pass ids for creates")
        if (t.creator == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Create request should have creator");
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
        // TODO: Add client code
        // judgesViewClient.updateJVTargets()
        return ResponseEntity.ok(t)
    }

    /**
     * Update Target by id
     *
     * @param id Long id of the Target being updated
     * @param t T original target to be updated
     * @param other T target with new values
     * @return the updated Target as JSON
     */
    @Suppress("UNUSED_PARAMETER")
    fun update(id: Long?, t: T, other: T): ResponseEntity<Any> {
        val targetSightingDao = DAOFactory.getDAO(
            DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
            t.fetchAssociatedTargetSightingClass()) as TargetSightingsDatabaseAccessor<out TargetSighting>

        if (other.id !== null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't pass ids for updates")
        }
        if (other.creator !== null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't pass creator for updates")
        }

        val isTSIdUpdated = t.getthumbnail_tsid() != other.getthumbnail_tsid()

        t.updateFromTarget(other)
        if (other.geotag !== null) {
            targetSightingDao
                .getAllTargetSightingsForTarget(t.id)
                .forEach { it.geotag?.isManualGeotag = true }
        }
        getTargetDao().update(t)
        if (isTSIdUpdated && CUAIR_INTEROP_REQUESTS) {
            interopClient.updateTargetImage(targetSightingDao.get(t.getthumbnail_tsid()))
        }
        //interopClient.updateTarget(getTargetDao().get(t.id))

        // TODO: Add client code
        // if (CUAIR_INTEROP_REQUESTS) {
        //     if (isTSIdUpdated) {
        //         interopClient.updateTargetImage(targetSightingDao.get(t.thumbnail_tsid))
        //     }
        //     interopClient.updateTarget(getTargetDao().get(t.id))
        // }

        // judgesViewClient.updateJVTargets()
        return ResponseEntity.ok(t)
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

        if (t === null) return ResponseEntity.noContent().build()

        val targetSightingDao = DAOFactory.getDAO(
            DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
            t.fetchAssociatedTargetSightingClass()) as TargetSightingsDatabaseAccessor<out TargetSighting>

        // TODO: Add client code
        if (CUAIR_INTEROP_REQUESTS) interopClient.attemptDelete(t)

        targetSightingDao.unassociateAllTargetSightingsForTarget(id)
        getTargetDao().delete(id)
        // TODO: Add client code
        // judgesViewClient.updateJVTargets()
        return ResponseEntity.ok(t)
    }

    companion object {
        // TODO: Add client code

        // private val judgesViewClient = ClientFactory.getJudgesViewClient()

        /** The interop client for communication with the competition server */
        private val interopClient = ClientFactory.getInteropClient()

        /** A logger */
        private val logger = LoggerFactory.getLogger(TargetController::class.java)
        private val CUAIR_INTEROP_REQUESTS = Flags.CUAIR_INTEROP_REQUESTS;
    }
}
