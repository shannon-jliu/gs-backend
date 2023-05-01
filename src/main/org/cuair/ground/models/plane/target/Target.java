package org.cuair.ground.models.plane.target;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cuair.ground.models.ClientCreatable;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.util.Flags;

/** Model to represent the target, which is an object on the field. */
@MappedSuperclass
public abstract class Target extends ClientCreatable {

  /**
   * Represents the Geotag of this target that records the gps location and the
   * direction that the
   * target is facing
   */
  @OneToOne(cascade = CascadeType.ALL)
  protected Geotag geotag;

  /** Represents the id for this target on the competition server */
  @JsonIgnore
  private Long judgeTargetId;

  /** Id of target sighting used for thumbnail */
  // @Column(name = "thumbnailTSId")
  private Long thumbnailTsid;

  /** Id of airdrop target from 0-4 */
  private Long airdropId;

  /**
   * Creates a target
   *
   * @param creator       the ODLCUser that created this Target
   * @param geotag        Geotag of this Target
   * @param judgeTargetId Long id of this target on the competition server
   * @param thumbnailTsid Long id of Target Sighting used for thumbnail
   * @param airdropId     Long id of the corresponding airdrop target
   */
  public Target(ODLCUser creator, Geotag geotag, Long judgeTargetId, Long thumbnailTsid, Long airdropId) {
    super(creator);
    this.geotag = geotag;
    this.judgeTargetId = judgeTargetId;
    this.thumbnailTsid = thumbnailTsid;
    this.airdropId = airdropId;
  }

  /**
   * Given another target, it updates all fields of this instance if there are any
   * differences
   *
   * @param other Target containing updated fields
   */
  public void updateFromTarget(Target other) {
    if (other.getthumbnailTsid() != null) {
      this.thumbnailTsid = other.getthumbnailTsid();
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
   * @param geotag Geotag representing the new location and direction of the
   *               Target
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
   * Sets the id of the target on the competition server. The judge target id
   * should never be changed after initial
   * assignment which is done elsewhere, so this should only be used in tests
   *
   * @param judgeTargetId new judge target id
   */
  public void setJudgeTargetId_CREATION(Long judgeTargetId) {
    this.judgeTargetId = judgeTargetId;
  }

  /**
   * Gets the id of the target sighting used for thumbnail
   *
   * @return Long thumnail target sighting id
   */
  public Long getthumbnailTsid() {
    return thumbnailTsid;
  }

  /**
   * Sets the id of the target sighting used for thumbnail
   *
   * @param thumbnailTsid thumbnail target sighting id
   */
  public void setthumbnailTsid(Long thumbnailTsid) {
    this.thumbnailTsid = thumbnailTsid;
  }

  /**
   * Gets the airdrop id of the target
   *
   * @return int airdrop id
   */
  public Long getAirdropId() { return airdropId; }

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
  public boolean equals(Object o) {
    Target other = (Target) o;

    if (!super.equals(other))
      return false;

    if (!Objects.deepEquals(this.geotag, other.getGeotag()))
      return false;

    if (!Objects.deepEquals(this.judgeTargetId, other.getJudgeTargetId()))
      return false;

    if (!Objects.deepEquals(this.airdropId, other.getAirdropId()))
      return false;

    return Objects.deepEquals(this.thumbnailTsid, other.getthumbnailTsid());
  }
}
