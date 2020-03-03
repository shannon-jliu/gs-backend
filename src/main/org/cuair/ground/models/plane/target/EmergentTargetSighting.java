package org.cuair.ground.models.plane.target;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.Confidence;
import org.cuair.ground.models.geotag.Geotag;

/** Emergent TargetSighting that has a description */
@Entity
public class EmergentTargetSighting extends TargetSighting {

    /** The target of this target sighting */
    @ManyToOne protected EmergentTarget target;

    /** Description of the emergent target sighting */
    private String description;

    /** @param description description of the emergent target sighting */
    public EmergentTargetSighting(
            ODLCUser creator,
            Integer pixel_x,
            Integer pixel_y,
            Integer width,
            Integer height,
            Geotag geotag,
            EmergentTarget target,
            String description,
            Double radiansFromTop,
            Double orientationConfidence,
            Confidence mdlcClassConf,
            Assignment assignment) {
        super(
            creator,
            pixel_x,
            pixel_y,
            width,
            height,
            geotag,
            radiansFromTop,
            orientationConfidence,
            mdlcClassConf,
            assignment);
        this.target = target;
        this.description = description;
    }

    /**
     * Gets the description of the target sighting
     *
     * @return String description describes the target sighting
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the target
     *
     * @return Target
     */
    @Override
    public EmergentTarget getTarget() {
        return target;
    }

    /**
     * Sets the target
     *
     * @param target new Target
     */
    public void setTarget(EmergentTarget target) {
        this.target = target;
    }

    /** Resets the Target for this target sighting */
    @Override
    public void makeAssociatedTargetNull() {
        this.target = null;
    }

    /**
     * Given another target sighting, it updates all fields of this instance if there are any
     * differences
     *
     * @param other TargetSighting to update from
     */
    @Override
    public void updateFromTargetSighting(TargetSighting other) {
        super.updateFromTargetSighting(other);
        EmergentTargetSighting er;

        assert other instanceof EmergentTargetSighting;

        er = (EmergentTargetSighting) other;

        if (er.getDescription() != null) {
            this.description = er.getDescription();
        }

        this.target = er.getTarget();
    }

    /**
     * Determines if the given object is logically equal to this AlphanumTargetSighting
     *
     * @param o The object to compare
     * @return True if the object equals this QRTargetSighting
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        EmergentTargetSighting other = (EmergentTargetSighting) o;

        if (!Objects.deepEquals(this.description, other.getDescription())) {
            return false;
        }

        if (!Objects.deepEquals(this.target, other.getTarget())) {
            return false;
        }
        return true;
    }
}
