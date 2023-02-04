package org.cuair.ground.models.geotag

import org.cuair.ground.daos.ClientCreatableDatabaseAccessor
import org.cuair.ground.daos.DAOFactory
import org.cuair.ground.daos.DAOFactory.Companion.getDAO
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor
import org.cuair.ground.models.CUAirModel
import org.cuair.ground.models.plane.target.AlphanumTarget
import org.cuair.ground.models.plane.target.AlphanumTargetSighting
import org.cuair.ground.models.plane.target.EmergentTarget
import org.cuair.ground.models.plane.target.EmergentTargetSighting
import org.cuair.ground.models.plane.target.TargetSighting
import org.cuair.ground.models.plane.target.Target
import org.cuair.ground.util.Geotagging
import org.slf4j.LoggerFactory
import java.util.Objects
import javax.persistence.Embedded
import javax.persistence.Entity

/** Represents the position and orientation of an object on the ground  */
@Entity
class Geotag(
    /** The GPS coordinates of this geotag  */
    @Embedded
    var gpsLocation: GpsLocation?,
    /** The orientation of this geotag represented as radians from north clockwise (NE is clockwise from N, etc.)  */
    var clockwiseRadiansFromNorth: Double?
) : CUAirModel() {

    /**
     * Creates a new geotag with the target sighting
     *
     * @param sighting The TargetSighting of this geotag
     */
    constructor(sighting: TargetSighting?) : this(null, null) {
        if (sighting == null) {
            return
        }
        val assignment = sighting.assignment ?: return
        val image = assignment.image ?: return
        val telemetry = image.telemetry
        val fov = image.fov
        val gps = telemetry.getGps()
        val altitude = telemetry.getAltitude()
        val pixelx = sighting.getpixelx().toDouble()
        val pixely = sighting.getpixely().toDouble()
        val planeRoll = telemetry.getGimOrt().getRoll() * Math.PI / 180
        val planePitch = telemetry.getGimOrt().getPitch() * Math.PI / 180
        val planeYaw = telemetry.getPlaneYaw() * Math.PI / 180
        val centerLongitude = gps.getLongitude()
        val centerLatitude = gps.getLatitude()
        gpsLocation = Geotagging
                .getPixelCoordinates(centerLatitude, centerLongitude, altitude, fov, pixelx, pixely, planeRoll, planePitch, planeYaw)
        clockwiseRadiansFromNorth = Geotagging.calculateClockwiseRadiansFromNorth(planeYaw, sighting.radiansFromTop)
    }

    /**
     * Determines if the given object is logically equal to this Geotag
     *
     * @param other The object to compare
     * @return True if the object equals this Geotag
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Geotag) return false
        if (Radian.equals(clockwiseRadiansFromNorth, other.clockwiseRadiansFromNorth)) {
            return false
        }
        return Objects.deepEquals(gpsLocation, other.gpsLocation)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GpsLocation::class.java)
        /** The database access object for the assignment table  */
        private val alphaTargetSightingDao = getDAO(
                DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
                AlphanumTargetSighting::class.java) as TargetSightingsDatabaseAccessor<AlphanumTargetSighting?>
        private val emergentTargetSightingDao = getDAO<EmergentTargetSighting>(
                DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
                EmergentTargetSighting::class.java) as TargetSightingsDatabaseAccessor<EmergentTargetSighting?>
        private val alphanumTargetDao = getDAO(
                DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, AlphanumTarget::class.java) as ClientCreatableDatabaseAccessor<AlphanumTarget?>
        private val emergentTargetDao = getDAO(
                DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget::class.java) as ClientCreatableDatabaseAccessor<EmergentTarget?>

        /**
         * Checks to see if geotag can be set for target sighting
         *
         * @param sighting the target sighting
         */
        private fun canSetGeotag(sighting: TargetSighting): Boolean {
            if (sighting is AlphanumTargetSighting) {
                if (sighting.isOffaxis) {
                    return false
                }
            }
            val assignment = sighting.assignment ?: return false
            assignment.image ?: return false
            return true
        }

        /**
         * Updates the Geotag of a target based on the Geotag of its corresponding TargetSightings
         *
         * @param targ the target
         * @param ts   the target sighting
         */
        @JvmStatic
        fun updateGeotag(targ: Target, ts: TargetSighting?) {
            val sights: MutableList<TargetSighting?> = ArrayList()
            when {
                targ.fetchAssociatedTargetSightingClass() == AlphanumTargetSighting::class.java -> {
                    sights.addAll(alphaTargetSightingDao.getAllTargetSightingsForTarget(targ.id))
                }
                targ.fetchAssociatedTargetSightingClass() == EmergentTargetSighting::class.java -> {
                    sights.addAll(emergentTargetSightingDao.getAllTargetSightingsForTarget(targ.id))
                }
                else -> {
                    logger.warn("Target class is not Alphanum or Emergent")
                }
            }
            if (ts != null && !sights.contains(ts)) {
                sights.add(ts)
            }
            val sightsArr = sights.toTypedArray()
            val geotags: Array<Geotag?> = sightsArr.mapNotNull { obj: TargetSighting? -> obj?.geotag }.toTypedArray()
            targ.geotag = Geotagging.median(*geotags)
            updateTargetInDao(targ)
        }

        /**
         * Checks if geotag from TargetSighting's assignment is valid. If so, sets target sighting's
         * geotag to it.
         *
         * @param ts
         * @return true if geotag was set, false otherwise
         */
        @JvmStatic
        fun attemptSetGeotagForTargetSighting(ts: TargetSighting): Boolean {
            if (!canSetGeotag(ts)) {
                return false
            }
            ts.geotag = Geotag(ts)
            return true
        }

        /**
         * Update target's dao
         *
         * @param targ the target
         */
        private fun updateTargetInDao(targ: Target) {
            if (targ.fetchAssociatedTargetSightingClass() == AlphanumTargetSighting::class.java) {
                if (!alphanumTargetDao.create(targ as AlphanumTarget)) {
                    alphanumTargetDao.update(targ)
                }
            } else if (targ.fetchAssociatedTargetSightingClass() == EmergentTargetSighting::class.java) {
                emergentTargetDao.update(targ as EmergentTarget)
            } else {
                logger.warn("Target class is not Alphanum or Emergent")
            }
        }
    }
}
