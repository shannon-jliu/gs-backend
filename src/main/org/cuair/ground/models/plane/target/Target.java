package org.cuair.ground.models.plane.target;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import org.cuair.ground.models.ClientCreatable;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.geotag.Geotag;

/** Model to represent the target, which is an object on the field. */
@MappedSuperclass
public abstract class Target extends ClientCreatable {

    /**
     * Represents the Geotag of this target that records the gps location and the direction that the
     * target is facing
     */
    @OneToOne(cascade = CascadeType.ALL)
    protected Geotag geotag;

    /** Represents the id for this target on the competition server */
    @JsonIgnore private Long judgeTargetId;

    /** Id of target sighting used for thumbnail */
    private Long thumbnailTSId;

    /**
     * Creates a target
     *
     * @param creator the ClientType of the Target
     * @param geotag Geotag of this Target
     * @param judgeTargetId Long id of this target on the competition server
     * @param thumbnailTSId Long id of Target Sighting used for thumbnail
     */
    public Target(ClientType creator, Geotag geotag, Long judgeTargetId, Long thumbnailTSId) {
        super(creator);
        this.geotag = geotag;
        this.judgeTargetId = judgeTargetId;
        this.thumbnailTSId = thumbnailTSId;
    }

    /**
     * Given another target, it updates all fields of this instance if there are any differences
     *
     * @param other Target containing updated fields
     */
    public void updateFromTarget(Target other) {
        if (other.getThumbnailTSId() != null) {
            this.thumbnailTSId = other.getThumbnailTSId();
        }
    }

    /**
     * Converts this object to a Json according to the judges specification
     *
     * @return JsonNode
     */
    public abstract JsonNode toJson();

    /**
     * Returns the class of targetSighting associated with this target
     *
     * @return Class<? extends TargetSighting> associated with target
     */
    public abstract Class<? extends TargetSighting> fetchAssociatedTargetSightingClass();

    /**
     * Gets the Geotag of this Target
     *
     * @return Geotag representing the location and direction of the Target
     */
    public Geotag getGeotag() {
        return geotag;
    }

    /**
     * Sets the Geotag of this Target
     *
     * @param geotag Geotag representing the new location and direction of the Target
     */
    public void setGeotag(Geotag geotag) {
        this.geotag = geotag;
    }

    /**
     * Gets the id of this target on the competition server
     *
     * @return Long target id
     */
    public Long getJudgeTargetId() {
        return judgeTargetId;
    }

    /**
     * Sets the id of this target on the competition server
     *
     * @param judgeTargetId Long new target id
     */
    public void setJudgeTargetId(Long judgeTargetId) {
        this.judgeTargetId = judgeTargetId;
    }

    /**
     * Gets the id of the target sighting used for thumbnail
     *
     * @return Long thumnail target sighting id
     */
    public Long getThumbnailTSId() {
        return thumbnailTSId;
    }

    /**
     * Sets the id of the target sighting used for thumbnail
     *
     * @param thumbnailTSId thumbnail target sighting id
     */
    public void setThumbnailTSId(Long thumbnailTSId) {
        this.thumbnailTSId = thumbnailTSId;
    }

    /**
     * Gets the String representation of the target type
     *
     * @return String the type
     */
    @JsonIgnore
    public abstract String getTypeString();

    /**
     * Determines if the given object is logically equal to this Target
     *
     * @param o The object to compare
     * @return True if the object equals this Target
     */
    @Override
    public boolean equals(@NotNull Object o) {
        Target other = (Target) o;

        if (!super.equals(other)) {
          return false;
        }

        if (!Objects.deepEquals(this.geotag, other.getGeotag())) {
          return false;
        }

        if (!Objects.deepEquals(this.judgeTargetId, other.getJudgeTargetId())) {
          return false;
        }

        if (!Objects.deepEquals(this.thumbnailTSId, other.getThumbnailTSId())) {
          return false;
        }
        return true;
    }
}
