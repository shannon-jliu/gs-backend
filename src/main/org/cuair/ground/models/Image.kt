package org.cuair.ground.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.cuair.ground.models.geotag.Telemetry
import java.util.Objects
import javax.persistence.Entity
import javax.persistence.OneToOne
import javax.persistence.CascadeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ObjectMapper

import io.ebean.annotation.EnumValue
import com.fasterxml.jackson.annotation.JsonProperty

/** Represents an image and its corresponding metadata as sent down from the plane */
@Entity
class Image(
        /** The URL where clients can retrieve the image file  */
        var imageUrl: String?,
        /** Closest Telemetry for when this Image was taken  */
        @OneToOne(cascade = [CascadeType.ALL])
        var telemetry: Telemetry,
        /** The type of this image. Either FIXED, TRACKING, or OFFAXIS */
        var imgMode: ImgMode,
        /** True if has at least one associated MDLC assignment, otherwise false. */
        @JsonIgnore var hasMdlcAssignment: Boolean = false,
        /** True if has at least one associated ADLC assignment, otherwise false. */
        @JsonIgnore var hasAdlcAssignment: Boolean = false,
        /** The horizontal field of fiew of this image in degrees. */
        var fov: Double
) : TimestampModel() {

    /** Secondary constructor that sets imgMode to FIXED by default */
    constructor(
            imageUrl: String,
            telemetry: Telemetry,
            fov: Double
    ) : this(imageUrl, telemetry, ImgMode.FIXED, false, false, fov)

    /** The filesystem path this image lives on relative to the server directory */
    @Transient var localImageUrl: String? = null

    /** The possible image modes: fixed, tracking, and off-axis */
    enum class ImgMode (val mode: String) {
        @JsonProperty("fixed") @EnumValue("0") FIXED("fixed"),
        @JsonProperty("tracking") @EnumValue("1") TRACKING("tracking"),
        @JsonProperty("off-axis") @EnumValue("2") OFFAXIS("off-axis")
    }

    /**
     * TODO
     * 
     * Internal method for finding geotags for a given image id
     *
     * @return ObjectNode
     */
    @Suppress("DEPRECATION")
    @JsonIgnore fun getLocations(): ObjectNode? {
        val mapper = ObjectMapper()
        val locs = mapper.createObjectNode() as ObjectNode
        return locs
    }

    /**
     * Determines if the given object is logically equal to this Image
     *
     * @param other The object to compare
     * @return true if the object equals this Image
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Image) return false

        if (this.imageUrl != other.imageUrl) return false
        if (this.imgMode != other.imgMode) return false
        if (!Objects.equals(this.telemetry, other.telemetry)) return false
        if (this.fov != other.fov) return false
        return true
    }
}
