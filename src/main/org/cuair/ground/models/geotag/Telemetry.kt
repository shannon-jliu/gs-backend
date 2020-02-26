package org.cuair.ground.models.geotag

import java.util.Objects
import javax.persistence.Entity
import javax.persistence.Embedded
import org.cuair.ground.models.CUAirModel
import org.cuair.ground.models.geotag.GimbalOrientation

/** Represents telemetry of an object
    gps will contain latitude and longitude
    altitude is in meters
    planeYaw is in degrees with 0 at north and increasing in the clockwise direction
    gimOrt is the gimbal orientation with pitch and roll
    */
@Entity
class Telemetry(
        @Embedded
        private var gps: GpsLocation,
        private var altitude: Double,
        private var planeYaw: Double,
        private var pitch: Double,
        private var roll: Double
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

    fun getPitch(): Double {
        return pitch;
    }

    fun getRoll(): Double {
        return roll;
    }

    /**
     * Get the gimbal orientation of this Telemetry instance
     *
     * @return the gimbal orientation of this Telemetry instance
     */
    // fun getGimOrt(): GimbalOrientation {
    //     return gimOrt;
    // }

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

    fun setPitch(pitch: Double) {
        this.pitch = pitch;
    }

    fun setRoll(roll: Double) {
        this.roll = roll;
    }

    /**
     * Change the planeYaw of this Telemetry instance
     *
     * @param planeYaw The new plane yaw for this Telemetry instance
     */
    fun setPlaneYaw(planeYaw: Double) {
        this.planeYaw = planeYaw;
    }

    // fun setGimbalOrientation(gimbalOrientation: GimbalOrientation) {
    //     this.gimbalOrientation = gimbalOrientation;
    // }

    // TODO: Add the rest of the methods

    /**
     * Change the gimbal orientation of this Telemetry instance
     *
     * @param gimOrt The new gimbal orientation for this Telemetry instance
     */
    // fun setGimOrt(gimOrt: GimbalOrientation) {
    //     this.gimOrt = gimOrt;
    // }

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
        //if (this.gimbalOrientation != other.gimbalOrientation) return false
        if (!Objects.equals(this.gps, other.gps)) return false
        //if (!Objects.equals(this.gimOrt, other.gimOrt)) return false
        return true
    }
}
