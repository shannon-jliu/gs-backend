package org.cuair.ground.models

import org.cuair.ground.models.geotag.Telemetry
import java.util.Objects
import javax.persistence.Entity
import javax.persistence.OneToOne
import javax.persistence.CascadeType

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
        /** The horizontal field of fiew of this image in degrees. */
        var fov: Double,
        /** True if has at least one associated assignment, otherwise false. */
        var hasAssignment: Boolean = false
) : TimestampModel() {

    /** Secondary constructor that sets imgMode to FIXED by default */
    constructor(
            imageUrl: String,
            telemetry: Telemetry,
            fov: Double
    ) : this(imageUrl, telemetry, ImgMode.FIXED, fov,false)

    /** The filesystem path this image lives on relative to the server directory */
    @Transient var localImageUrl: String? = null

    /** The possible image modes: fixed, tracking, and off-axis */
    enum class ImgMode (val mode: String) {
        @JsonProperty("fixed") @EnumValue("0") FIXED("fixed"),
        @JsonProperty("tracking") @EnumValue("1") TRACKING("tracking"),
        @JsonProperty("off-axis") @EnumValue("2") OFFAXIS("off-axis")
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
