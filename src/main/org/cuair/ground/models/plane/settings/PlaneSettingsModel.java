package org.cuair.ground.models.plane.settings;

import io.ebean.annotation.EnumValue;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.persistence.MappedSuperclass;
import org.cuair.ground.models.PlaneModel;

// todo implement this

/**
 * Base class for all settings that get sent to the plane. All Settings and models should extend
 * this class. It consists of a timestamp indicating when it was created.
 */
@MappedSuperclass
public class PlaneSettingsModel extends PlaneModel {
    
    public enum CameraType {
        IDS,
        ZCAM
    }
    
    public enum PlaneModelStatus {
        // model was successfully sent to the plane
        @EnumValue("sent")
        sent,
        
        // model was successfully sent to the plane but was invalid
        @EnumValue("failed")
        failed,
        
        // model is queued to be sent to the plane
        @EnumValue("queued")
        queued
    }
    
    /** Status enum to indicate whether or not the model was sent to the plane */
    private PlaneModelStatus status = PlaneModelStatus.queued;
    
    /**
     * Gets the state of whether or not the model was sent to the plane
     *
     * @return the boolean state of whether or not the model was sent
     */
    public PlaneModelStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the status of whether or not the model was sent to the plane
     *
     * @param status the status of whether or not the model was sent
     */
    public void setStatus(PlaneModelStatus status) {
        this.status = status;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !super.equals(o)) {
            return false;
        }
        PlaneSettingsModel other = (PlaneSettingsModel) o;
        
        return Objects.deepEquals(this.status, other.getStatus());
    }
}
