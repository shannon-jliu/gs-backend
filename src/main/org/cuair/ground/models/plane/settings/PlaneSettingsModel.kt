package org.cuair.ground.models.plane.settings

import io.ebean.Ebean
import io.ebean.annotation.EnumValue
import java.util.Objects
import javax.persistence.MappedSuperclass
import org.cuair.ground.models.plane.PlaneModel

/**
 * Base class for all settings that get sent to the plane. All Settings and models should extend
 * this class. It consists of a timestamp indicating when it was created.
 */
@MappedSuperclass
open class PlaneSettingsModel : PlaneModel() {
    public enum class PlaneModelStatus(val status: String) {
        // model was successfully sent to the plane
        @EnumValue("0")
        sent("sent"),

        // model was successfully sent to the plane but was invalid
        @EnumValue("1")
        failed("failed"),

        // model is queued to be sent to the plane
        @EnumValue("2")
        queued("queued")
    }

    /** Status enum to indicate whether or not the model was sent to the plane */
    private var status: PlaneModelStatus = PlaneModelStatus.queued

    /**
     * Gets the state of whether or not the model was sent to the plane
     *
     * @return the boolean state of whether or not the model was sent
     */
    fun getStatus(): PlaneModelStatus {
        return status
    }

    /**
     * Sets the status of whether or not the model was sent to the plane
     *
     * @param status the status of whether or not the model was sent
     */
    fun setStatus(status: PlaneModelStatus): Unit {
        this.status = status
    }

    override fun equals(o: Any?): Boolean {
        if (o == null || !super.equals(o)) {
            return false
        }
        val other: PlaneSettingsModel = o as PlaneSettingsModel

        return Objects.deepEquals(this.status, other.getStatus())
    }
}
