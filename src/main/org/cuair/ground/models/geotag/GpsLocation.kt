package org.cuair.ground.models.geotag

import java.util.Objects
import javax.persistence.Basic
import javax.persistence.Embeddable
import javax.validation.constraints.NotNull
import org.cuair.ground.models.exceptions.InvalidGpsLocationException
import kotlin.math.abs

/** Represents a GPS location in the world */
@Embeddable
class GpsLocation
    /**
     * Creates a new GPS location with the given latitude and longitude
     *
     * @param latitude The latitude of the GPS location
     * @param longitude The longitude of the GPS location
     * @throws InvalidGpsLocationException If latitude is not in range [-90.0,90.0]
     * @throws InvalidGpsLocationException If longitude is not in range [-180.0,180.0]
     */
    @Throws(InvalidGpsLocationException::class)
    constructor(
        @field:Basic(optional = true)
        private var latitude: Double,
        @field:Basic(optional = true)
        private var longitude: Double
    ) {
        init {
            if (latitude != null && abs(latitude!!) > ABS_LATITUDE_BOUND) {
                throw InvalidGpsLocationException(
                    "Latitude should be within -$ABS_LATITUDE_BOUND and $ABS_LATITUDE_BOUND"
                )
            }
            if (longitude != null && abs(longitude!!) > ABS_LONGITUDE_BOUND) {
                throw InvalidGpsLocationException(
                    "Longitude should be within -$ABS_LONGITUDE_BOUND and $ABS_LONGITUDE_BOUND"
                )
            }
        }

    /**
     * Get the latitude of this GPS location
     *
     * @return the latitude of this GPS location
     */
    fun getLatitude(): Double {
        return latitude
    }

    /**
     * Change the latitude of this GPS location
     *
     * @param latitude The new latitude for this GPS location
     * @throws InvalidGpsLocationException If latitude is not in range [-90.0,90.0]
     */
    @Throws(InvalidGpsLocationException::class)
    fun setLatitude(latitude: Double) {
        if (latitude != null && abs(latitude) > ABS_LATITUDE_BOUND) {
            throw InvalidGpsLocationException(
                "Latitude should be within -$ABS_LATITUDE_BOUND and $ABS_LATITUDE_BOUND"
            )
        }
        this.latitude = latitude
    }

    /**
     * Get the longitude of this GPS location
     *
     * @return the longitude of this GPS location
     */
    fun getLongitude(): Double {
        return longitude
    }

    /**
     * Change the longitude of this GPS location
     *
     * @param longitude The new longitude for this GPS location
     * @throws InvalidGpsLocationException If longitude is not in range [-180.0,180.0]
     */
    @Throws(InvalidGpsLocationException::class)
    fun setLongitude(longitude: Double) {
        if (longitude != null && abs(longitude) > ABS_LONGITUDE_BOUND) {
            throw InvalidGpsLocationException(
                "Longitude should be within -$ABS_LONGITUDE_BOUND and $ABS_LONGITUDE_BOUND"
            )
        }
        this.longitude = longitude
    }

    /**
     * Determines if the given object is logically equal to this GPS location
     *
     * @param other The object to compare
     * @return True if the object equals this GPS location
     */
    override fun equals(@NotNull other: Any?):Boolean {
        if(other !is GpsLocation) return false
        if (this.latitude != other.latitude) return false
        if (this.longitude != other.longitude) return false
        return true
    }

    // TODO add in euclidean distance

    companion object {
        /** Maximum valid latitude  */
        const val ABS_LATITUDE_BOUND = 90.0
        /** Maximum valid longitude  */
        const val ABS_LONGITUDE_BOUND = 180.0

        // TODO add in the rest of these methods later
    }
}
