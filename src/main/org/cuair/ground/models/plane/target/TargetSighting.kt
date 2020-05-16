package org.cuair.ground.models.plane.target

import org.cuair.ground.models.Assignment
import org.cuair.ground.models.ClientCreatable
import org.cuair.ground.models.Confidence
import org.cuair.ground.models.ODLCUser
import org.cuair.ground.models.geotag.Geotag
import java.util.Objects
import javax.persistence.CascadeType
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.OneToOne

/**
 * Model to represent a target sighting. The target sighting is the sighting of a target in a
 * specific image.
 */
@MappedSuperclass
abstract class TargetSighting(
        creator: ODLCUser?,
        /** The x pixel coordinate of the center of the target sighting in the specific Image  */
        var pixelx: Int? = 0,
        /** The y pixel coordinate of the center of the target sighting in the specific Image  */
        var pixely: Int? = 0,
        /** The horizontal pixel width of the target sighting in the specific image  */
        var width: Int?,
        /** The vertical pixel height of the target sighting in the specific image  */
        var height: Int?,
        /**
         * Represents the Geotag of this target sighting that records the gps location and the direction
         * that the target sighting is facing
         */
        @field:OneToOne(cascade = [CascadeType.ALL]) var geotag: Geotag?,
        /**
         * The orientation of the target sighting with respect to the top of the image. This means that
         * the vector below is 0 and the radians increase in a counterclockwise fashion.
         */
        val radiansFromTop: Double?,
        /** The confidence the vision system has in the target orientation identification  */
        open var orientationConfidence: Double?,
        /** The confidence MDLC taggers have in the classification accuracy  */
        var mdlcClassConf: Confidence?,
        /**
         * The assignment from which this target sighting was created (contains the image that this target
         * sighting was tagged in)
         */
        @field:ManyToOne var assignment: Assignment?
) : ClientCreatable(creator) {
    /**
     * Given another target sighting, it updates all fields of this instance if there are any
     * differences
     *
     * @param other TargetSighting containing updated fields
     */
    open fun updateFromTargetSighting(other: TargetSighting) {
        assert(assignment != null)
        if (other.pixelx != null) {
            pixelx = other.pixelx
        }
        if (other.pixely != null) {
            pixely = other.pixely
        }
        if (other.width != null) {
            width = other.width
        }
        if (other.height != null) {
            height = other.height
        }
        if (other.geotag != null) {
            geotag = other.geotag
        }
        if (other.orientationConfidence != null) {
            orientationConfidence = other.orientationConfidence
        }
        if (other.mdlcClassConf != null) {
            mdlcClassConf = other.mdlcClassConf
        }
        if (other.assignment != null) {
            assignment = other.assignment
        }
    }

    /** Sets this target to be null  */
    abstract fun makeAssociatedTargetNull()

    abstract val target: Target?

    /**
     * Determines if the given object is logically equal to this AlphanumTargetSighting
     *
     * @param other The object to compare
     * @return True if the object equals this AlphanumTargetSighting
     */
    override fun equals(other: Any?): Boolean {
        if (other !is TargetSighting || !super.equals(other)) return false
        if (!Objects.deepEquals(geotag, other.geotag)) return false
        if (!Objects.deepEquals(pixelx, other.pixelx)) return false
        if (!Objects.deepEquals(pixely, other.pixely)) return false
        if (!Objects.deepEquals(width, other.width)) return false
        if (!Objects.deepEquals(height, other.height)) return false
        if (!Objects.deepEquals(radiansFromTop, other.radiansFromTop)) return false
        if (!Objects.deepEquals(orientationConfidence, other.orientationConfidence)) return false
        return if (!Objects.deepEquals(mdlcClassConf, other.mdlcClassConf)) false else Objects.deepEquals(assignment, other.assignment)
    }

}
