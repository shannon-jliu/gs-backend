package org.cuair.ground.models

import org.cuair.ground.models.geotag.Telemetry
import org.cuair.ground.models.geotag.Geotag
import java.util.Objects
import javax.persistence.Entity
import javax.persistence.OneToOne
import javax.persistence.CascadeType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ObjectMapper

/** Represents an image and its corresponding metadata as sent down from the plane */
@Entity
class Image(
        /** The URL where clients can retrieve the image file  */
        var imageUrl: String,
        /** Closest Telemetry for when this Image was taken  */
        @OneToOne(cascade = [CascadeType.ALL])
        var telemetry: Telemetry?,
        /** The type of this image. Either FIXED, TRACKING, or OFFAXIS */
        var imgMode: ImgMode
) : TimestampModel() {

    /** Secondary constructor that sets imgMode to FIXED by default */
    constructor(
            imageUrl: String,
            telemetry: Telemetry?
    ) : this(imageUrl, telemetry, ImgMode.FIXED)

    /** The filesystem path this image lives on relative to the server directory */
    @Transient var localImageUrl: String? = null

    /** The possible image modes: fixed, tracking, and off-axis */
    enum class ImgMode (val mode: String) {
        FIXED("fixed"),
        TRACKING("tracking"),
        OFFAXIS("off-axis")
    }

    /**
     * Internal method for finding geotags for a given image id
     *
     * @return ObjectNode
     */
    @Suppress("DEPRECATION")
    @JsonIgnore fun getLocations(): ObjectNode? {
        val imageTelemetry = this.telemetry ?: return null
        val imageGPS = imageTelemetry.getGps()
        val centerLatitude = imageGPS?.getLatitude()
        val centerLongitude = imageGPS?.getLongitude()
        val planeYaw = imageTelemetry.getPlaneYaw()?.times(Math.PI/180)
        val altitude = imageTelemetry.getAltitude()
        
        // TOOD: !! vs ?. --> And should this be done in the declarations above?
        val topLeft = Geotag.getPixelCoordinates(centerLatitude!!, centerLongitude!!, altitude!!, 0.0, 0.0, planeYaw!!)
        val topRight = Geotag.getPixelCoordinates(centerLatitude, centerLongitude, altitude, Geotag.IMAGE_WIDTH, 0.0, planeYaw)
        val bottomLeft = Geotag.getPixelCoordinates(centerLatitude, centerLongitude, altitude, 0.0, Geotag.IMAGE_HEIGHT, planeYaw)
        val bottomRight = Geotag.getPixelCoordinates(centerLatitude, centerLongitude, altitude, Geotag.IMAGE_WIDTH, Geotag.IMAGE_HEIGHT, planeYaw)

        val mapper = ObjectMapper()
        val locs = mapper.createObjectNode() as ObjectNode
        locs.put("topLeft", mapper.writeValueAsString(topLeft))
        locs.put("topRight", mapper.writeValueAsString(topRight))
        locs.put("bottomLeft", mapper.writeValueAsString(bottomLeft))
        locs.put("bottomRight", mapper.writeValueAsString(bottomRight))
        locs.put("orientation", mapper.writeValueAsString(planeYaw))
        locs.put("url", this.imageUrl)
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

        return true
    }
}
