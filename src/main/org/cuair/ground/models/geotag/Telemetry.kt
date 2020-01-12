package org.cuair.ground.models.geotag

import java.util.Objects
import javax.persistence.Entity
import javax.validation.constraints.NotNull
import org.cuair.ground.models.CUAirModel

/** Represents telemetry of an object
    altitude is in meters 
    planeYaw is in degrees with 0 at north and increasing in the clockwise direction
    orientation will contain gimbal roll/pitch
    */
@Entity
class Telemetry(
        private var gps: GpsLocation?,
        private var altitude: Double?,
        private var planeYaw: Double?,
        private var orientation : GimbalOrientation?
) : CUAirModel() {

    /**
     * Gets the gps this of telemetry data
     *
     * @return GpsLocation? The aerial position of this telemetry data
     */
    fun getGps(): GpsLocation? {
        return gps;
    }

    /**
     * Gets the altitude of this telemetry data
     *
     * @return Double? The aerial position of this telemetry data
     */
    fun getAltitude(): Double? {
        return altitude;
    }

    /**
     * Gets the heading of this telemetry data
     *
     * @return Double The heading of this telemetry data as radians from north
     */
    fun getPlaneYaw(): Double? {
        return planeYaw;
    }

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
        if (!Objects.equals(this.orientation, other.orientation)) return false
        if (!Objects.equals(this.gps, other.gps)) return false
        return true
    }
}
