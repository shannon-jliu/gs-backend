package org.cuair.ground.models.plane.settings

import javax.persistence.Entity
import javax.validation.constraints.NotNull

/** Model to represent the settings for the camera gimbal server  */
@Entity
class CameraGimbalSettings(val mode: CameraGimbalMode) : PlaneSettingsModel() {
    enum class CameraGimbalMode(val mode : String) {
        FIXED("fixed"),
        TRACKING("tracking"),
        IDLE("idle")
    }

    /**
     * Compares two instances of CameraGimbalSettings, true if equal, false if not
     *
     * @param other the object to compare
     * @return true if this camera gimbal settings is equal to the other
     */
    override fun equals(other: Any?): Boolean {
        if (other !is CameraGimbalSettings) return false
        return mode == other.mode
    }

}
