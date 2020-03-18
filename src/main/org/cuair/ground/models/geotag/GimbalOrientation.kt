package org.cuair.ground.models.geotag

import java.util.Objects
import javax.persistence.Basic
import javax.persistence.Embeddable
import javax.validation.constraints.NotNull
import org.cuair.ground.models.exceptions.InvalidGpsLocationException
import kotlin.math.abs

/** Represents the orientation of the gimbal */
@Embeddable
class GimbalOrientation
    /**
     * Creates a new GimbalOrientation given the pitch and roll in degrees
     *
     * @param pitch The pitch of the gimbal in degrees. 0 is pointing down and positive is pointing forward
     * @param roll The roll of the gimbal in degrees. 0 is pointing down and positive is pointing to the right
     */
    constructor(
        @field:Basic(optional = true)
        private var pitch: Double,
        @field:Basic(optional = true)
        private var roll: Double
    ) {

    /**
     * Get the pitch of this GimbalOrientation
     *
     * @return the pitch of this GimbalOrientation
     */
    fun getPitch(): Double {
        return pitch
    }

    /**
     * Change the pitch of this GimbalOrientation
     *
     * @param pitch The new pitch for this GimbalOrientation
     */
    fun setPitch(pitch: Double) {
        this.pitch = pitch
    }

    /**
     * Get the roll of this GimbalOrientation
     *
     * @return the roll of this GimbalOrientation
     */
    fun getRoll(): Double {
        return roll
    }

    /**
     * Change the roll of this GimbalOrientation
     *
     * @param roll The new roll for this GimbalOrientation
     */
    fun setRoll(roll: Double) {
        this.roll = roll
    }

    /**
     * Determines if the given object is logically equal to this GimbalOrientation
     *
     * @param other The object to compare
     * @return True if the object equals this GimbalOrientation
     */
    override fun equals(other: Any?):Boolean {
        if(other !is GimbalOrientation) return false
        if (this.pitch != other.pitch) return false
        if (this.roll != other.roll) return false
        return true
    }
}
