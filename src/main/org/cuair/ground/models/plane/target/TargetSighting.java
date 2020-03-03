package org.cuair.ground.models.plane.target;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ClientCreatable;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.Confidence;
import org.cuair.ground.models.geotag.Geotag;

/**
 * Model to represent a target sighting. The target sighting is the sighting of a target in a
 * specific image.
 */
@MappedSuperclass
public abstract class TargetSighting extends ClientCreatable {

    /**
     * The assignment from which this target sighting was created (contains the image that this target
     * sighting was tagged in)
     */
    @ManyToOne private Assignment assignment;

    /**
     * Represents the Geotag of this target sighting that records the gps location and the direction
     * that the target sighting is facing
     */
    @OneToOne(cascade = CascadeType.ALL)
    protected Geotag geotag;

    /** The x pixel coordinate of the center of the target sighting in the specific Image */
    protected Integer pixel_x;

    /** The y pixel coordinate of the center of the target sighting in the specific Image */
    protected Integer pixel_y;

    /** The horizontal pixel width of the target sighting in the specific image */
    protected Integer width;

    /** The vertical pixel height of the target sighting in the specific image */
    protected Integer height;

    /** The confidence MDLC taggers have in the classification accuracy */
    protected Confidence mdlcClassConf;

    /**
     * The orientation of the target sighting with respect to the top of the image. This means that
     * the vector below is 0 and the radians increase in a counterclockwise fashion.
     */
    private Double radiansFromTop;

    /** The confidence the vision system has in the target orientation identification */
    private Double orientationConfidence;

    /*
     * Creates a TargetSighting
     *
     * @param creator        the ODLCUser that created this Target Sighting
     * @param geotag         Geotag of this Target Sighting
     * @param pixel_x         Integer x pixel coordinate of the center of the
     *                       target sighting in the specific Image
     * @param pixel_y         Integer y pixel coordinate of the center of the
     *                       target sighting in the specific Image
     * @param radiansFromTop the orientation of the Target Sighting
     * @param
     * orientationConfidence the confidence the vision system has in the
     *                           target orientation identification
     * @param mdlcClassConf
     *                       the confidence MDLC taggers have in the target classification
     * @param assignment     the assignment that created this TargetSighting
     * @param width          the horizontal pixel width of the TargetSighting
     * @param height         the vertical pixel height of the TargetSighting
     *
     */
    public TargetSighting(
            ODLCUser creator,
            Integer pixel_x,
            Integer pixel_y,
            Integer width,
            Integer height,
            Geotag geotag,
            Double radiansFromTop,
            Double orientationConfidence,
            Confidence mdlcClassConf,
            Assignment assignment) {
        super(creator);
        this.pixel_x = pixel_x;
        this.pixel_y = pixel_y;
        this.width = width;
        this.height = height;
        this.geotag = geotag;
        this.radiansFromTop = radiansFromTop;
        this.orientationConfidence = orientationConfidence;
        this.mdlcClassConf = mdlcClassConf;
        this.assignment = assignment;
    }

    /**
     * Given another target sighting, it updates all fields of this instance if there are any
     * differences
     *
     * @param other TargetSighting containing updated fields
     */
    public void updateFromTargetSighting(TargetSighting other) {
        assert this.getAssignment() != null;

        if (other.getpixel_x() != null) {
            this.pixel_x = other.getpixel_x();
        }
        if (other.getpixel_y() != null) {
            this.pixel_y = other.getpixel_y();
        }
        if (other.getWidth() != null) {
            this.width = other.getWidth();
        }
        if (other.getHeight() != null) {
            this.height = other.getHeight();
        }
        if (other.getGeotag() != null) {
            this.geotag = other.getGeotag();
        }
        if (other.getOrientationConfidence() != null) {
            this.orientationConfidence = other.getOrientationConfidence();
        }
        if (other.getMdlcClassConf() != null) {
            this.mdlcClassConf = other.getMdlcClassConf();
        }
        if (other.getAssignment() != null) {
            this.assignment = other.getAssignment();
        }
    }

    /** Sets this target to be null */
    public abstract void makeAssociatedTargetNull();

    /**
     * Gets the target
     *
     * @return Target
     */
    public abstract Target getTarget();

    /**
     * Gets the x pixel coordinate of the TargetSighting
     *
     * @return pixel_x Integer x pixel coordinate
     */
    public Integer getpixel_x() {
        return pixel_x;
    }

    /**
     * Gets the y pixel coordinate of the TargetSighting
     *
     * @return Integer y pixel coordinate
     */
    public Integer getpixel_y() {
        return pixel_y;
    }

    /**
     * Gets the width of the TargetSighting in pixels
     *
     * @return width Integer pixel width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Gets the height of the TargetSighting in pixels
     *
     * @return height Integer pixel height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Gets the geotag of this TargetSighting
     *
     * @return Geotag representing the direction and location of the AlphanumTargetSighting
     */
    public Geotag getGeotag() {
        return geotag;
    }

    /**
     * Sets the geotag of this TargetSighting
     *
     * @param geotag Geotag representing the direction and location of the AlphanumTargetSighting
     */
    public void setGeotag(Geotag geotag) {
        this.geotag = geotag;
    }

    /**
     * Gets the assignment from which this target sighting was created
     *
     * @return the assignment for the target sighting
     */
    public Assignment getAssignment() {
        return assignment;
    }

    /**
     * Sets the assignment for this target sighting
     *
     * @param assignment the assignment to set for this target sighting
     */
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    /**
     * Gets the orientation of the target sighting from the top of the image
     *
     * @return Double representing the orientation of the sighting
     */
    public Double getRadiansFromTop() {
        return radiansFromTop;
    }

    /**
     * Gets the confidence the vision system has in the target orientation classification
     *
     * @return Double representing the target orientation confidence
     */
    public Double getOrientationConfidence() {
        return orientationConfidence;
    }

    /**
     * Sets the confidence the vision system has in the target orientation classification
     *
     * @return orientationConfidence Double representing the target orientation confidence
     */
    public void setOrientationConfidence(Double orientationConfidence) {
        this.orientationConfidence = orientationConfidence;
    }

    /**
     * Gets the confidence we have in the classification accuracy
     *
     * @return Confidence representing the classification confidence
     */
    public Confidence getMdlcClassConf() {
        return mdlcClassConf;
    }

    /**
     * Determines if the given object is logically equal to this AlphanumTargetSighting
     *
     * @param o The object to compare
     * @return True if the object equals this AlphanumTargetSighting
     */
    @Override
    public boolean equals(Object o) {
        TargetSighting other = (TargetSighting) o;

        if (!super.equals(o)) {
            return false;
        }

        if (!Objects.deepEquals(this.geotag, other.getGeotag())) {
            return false;
        }

        if (!Objects.deepEquals(this.pixel_x, other.getpixel_x())) {
            return false;
        }

        if (!Objects.deepEquals(this.pixel_y, other.getpixel_y())) {
            return false;
        }

        if (!Objects.deepEquals(this.width, other.getWidth())) {
            return false;
        }

        if (!Objects.deepEquals(this.height, other.getHeight())) {
            return false;
        }

        if (!Objects.deepEquals(this.radiansFromTop, other.getRadiansFromTop())) {
            return false;
        }

        if (!Objects.deepEquals(this.orientationConfidence, other.getOrientationConfidence())) {
            return false;
        }

        if (!Objects.deepEquals(this.mdlcClassConf, other.getMdlcClassConf())) {
            return false;
        }

        if (!Objects.deepEquals(this.assignment, other.getAssignment())) {
            return false;
        }

        return true;
    }
}
