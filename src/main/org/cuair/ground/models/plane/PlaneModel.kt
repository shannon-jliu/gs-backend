package org.cuair.ground.models.plane

import java.sql.Timestamp
import java.util.Objects
import org.cuair.ground.models.TimestampModel
import javax.persistence.MappedSuperclass

/**
 * Base class for all settings that get sent to the plane. All State models should extend this
 * class. It consists of a timestamp indicating when it was created.
 */
@MappedSuperclass
abstract class PlaneModel : TimestampModel() {

    /** A timestamp for the plane */
    private var planeTimestamp: Timestamp? = null

    /**
     * Retrieves the plane timestamp
     *
     * @return Timestamp timestamp
     */
    fun getPlaneTimestamp(): Timestamp? {
        return planeTimestamp
    }

    /**
     * Sets the timestamp
     *
     * @param planeTimestamp new timestamp
     */
    fun setPlaneTimestamp(planeTimestamp: Timestamp?): Unit {
        this.planeTimestamp = planeTimestamp
    }

    override fun equals(o: Any?): Boolean {
        if (o == null || !super.equals(o)) {
            return false
        }
        val other: PlaneModel = o as PlaneModel

        return Objects.deepEquals(this.planeTimestamp, other.getPlaneTimestamp());
    }
}
