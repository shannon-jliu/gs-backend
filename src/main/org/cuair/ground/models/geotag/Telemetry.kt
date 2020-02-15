package org.cuair.ground.models.geotag

import java.util.Objects
import javax.persistence.Entity
import javax.persistence.Embedded
import org.cuair.ground.models.CUAirModel

/** Represents telemetry of an object
    gps will contain latitude and longitude
    altitude is in meters
    planeYaw is in degrees with 0 at north and increasing in the clockwise direction
    */
@Entity
class Telemetry(
        @Embedded
        private var gps: GpsLocation,
        private var altitude: Double,
        private var planeYaw: Double
) : CUAirModel() {
    // TODO: Add the rest of the methods

    /**
     * Get the gps data of this Telemetry instance
     *
     * @return the gps data of this Telemetry instance
     */
    fun getGps(): GpsLocation {
        return gps;
    }

    /**
     * Get the altitude of this Telemetry instance
     *
     * @return the altitude of this Telemetry instance
     */
    fun getAltitude(): Double {
        return altitude;
    }

    /**
     * Get the plane yaw of this Telemetry instance
     *
     * @return the plane yaw of this Telemetry instance
     */
    fun getPlaneYaw(): Double {
        return planeYaw;
    }

    /**
     * Change the gps of this Telemetry instance
     *
     * @param gps The new gps for this Telemetry instance
     */
    fun setGps(gps: GpsLocation) {
        this.gps = gps;
    }

    /**
     * Change the altitude of this Telemetry instance
     *
     * @param altitude The new altitude for this Telemetry instance
     */
    fun setAltitude(altitude: Double) {
        this.altitude = altitude;
    }

    /**
     * Change the planeYaw of this Telemetry instance
     *
     * @param planeYaw The new plane yaw for this Telemetry instance
     */
    fun setPlaneYaw(planeYaw: Double) {
        this.planeYaw = planeYaw;
    }

    // TODO: Add the rest of the methods

    /**
     * Determines if the given object is logically equal to this Telemetry
     *
     * @param other The object to compare
     * @return true if the object equals this Telemetry
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Telemetry) return false

        if (this.altitude != other.altitude) return false
        if (this.planeYaw != other.planeYaw) return false
        if (!Objects.equals(this.gps, other.gps)) return false
        return true
    }
}
