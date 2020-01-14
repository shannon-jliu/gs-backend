package org.cuair.ground.models.geotag;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.cuair.ground.models.CUAirModel;
import org.cuair.ground.models.Image;

/** Model representing a manually geotagged image */
@Entity
public class MGTImage extends CUAirModel {

    /** Image corresponding to this MGT */
    @OneToOne
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private Image image;
    /**
     * Boolean flag indicating whether or not this MGT has been assigned to a manual geotagging client
     */
    private Boolean isSent;

    /** Boolean flag indicating whether or not this MGT has telemetry assigned to it */
    private Boolean hasTelemetry;

    /** Boolean flag indicating whether or not this MGT has been assigned a target */
    private Boolean hasTarget;

    public MGTImage(Image image) {
        this.image = image;
        this.isSent = false;
        this.hasTelemetry = false;
        this.hasTarget = false;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Returns whether or not this MGTImage has existing telemetry data or not
     *
     * @return true if there is telemetry, false otherwise
     */
    public Boolean hasTelemetry() {
        return hasTelemetry;
    }

    /** Updates this MGTImage to know that telemetry data exists */
    public void setHasTelemetry() {
        hasTelemetry = true;
    }

    /**
     * Returns whether or not this MGTImage has an associated target or not
     *
     * @return true if there is a target, false otherwise
     */
    public Boolean hasTarget() {
        return hasTarget;
    }

    /** Updates this MGTImage to know that it has an associated target */
    public void setHasTarget() {
        hasTarget = true;
    }

    /**
     * Returns whether or not this MGTImage was sent to the manualgeotagger or not
     *
     * @return True if it was sent, false otherwise
     */
    public Boolean isDone() {
        return isSent;
    }

    /**
     * Updates this MGTImage's status for whether it was sent to the manualgeotagger or not
     *
     * @param done The value to be updated with
     */
    public void setDone(Boolean done) {
        isSent = done;
    }
}
