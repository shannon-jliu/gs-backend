package org.cuair.ground.models.geotag

import java.util.Objects
import javax.persistence.Embeddable
import javax.validation.constraints.NotNull
import kotlin.math.abs

/**
 * Orientation of the gimbal. Includes roll and pitch in degrees, but not yaw. 0,0 is pointing down
 * Roll: Positive is to the left side of the plane
 * Pitch: Positive is towards the tail of the plane
 */
@Embeddable
class GimbalOrientation(var roll: Double, var pitch: Double) {

    /**
     * Determines if the given object is logically equal to this GimbalOrientation
     *
     * @param other The object to compare
     * @return true if the object equals this GimbalOrientation
     */
    override fun equals(@NotNull other: Any?): Boolean {
        if (other !is GimbalOrientation) return false

        if ((this.roll != other.roll) && abs(this.roll - other.roll) > ACCEPTABLE_ERROR) {
            return false
        }
        if ((this.pitch != other.pitch) && abs(this.pitch - other.pitch) > ACCEPTABLE_ERROR) {
            return false
        }
        return true
    }

    companion object {
        /** Acceptable threshold of error for equals() method */
        const val ACCEPTABLE_ERROR = 10E-5
    }
}
