package org.cuair.ground.models.plane.target;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import javax.persistence.Entity;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.util.Flags;

/** Emergent Target that is associated with an Emergent Target Sighting. Has a description. */
@Entity
public class EmergentTarget extends Target {

  /** Description of the emergent target sighting */
  private String description;

  /**
   * Creates an EmergentTarget
   *
   * @param description   description of the emergent target sighting
   * @param creator       the ODLCUser who created this Target
   * @param geotag        Geotag of this Target
   * @param judgeTargetId Long id of this Target on the competition server
   * @param thumbnailTsid Long id of Target Sighting used for thumbnail
   * @param airdropId     Long id of this Target's airdrop
   */
  public EmergentTarget(
      ODLCUser creator,
      Geotag geotag,
      String description,
      Long judgeTargetId,
      Long thumbnailTsid,
      Long airdropId) {
    super(creator, geotag, judgeTargetId, thumbnailTsid, airdropId);
    this.description = description;
  }

  /**
   * Gets the description for this emergent target sighting
   *
   * @return String description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Given another target sighting, it updates all fields of this instance if there are any
   * differences
   *
   * @param other
   */
  @Override
  public void updateFromTarget(Target other) {
    super.updateFromTarget(other);
    EmergentTarget er = null;

    if (!(other instanceof EmergentTarget)) {
      return;
    }
    er = (EmergentTarget) other;

    if (er.getDescription() != null) {
      this.description = er.getDescription();
    }
    if (er.getGeotag() != null) {
      this.geotag = er.getGeotag();
    }
  }

  /** Returns class associated with this target */
  @Override
  public Class<? extends TargetSighting> fetchAssociatedTargetSightingClass() {
    return EmergentTargetSighting.class;
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

    EmergentTarget other = (EmergentTarget) o;

    return Objects.deepEquals(this.description, other.getDescription());
  }

  @Override
  @JsonIgnore
  public String getTypeString() {
    return "Emergent";
  }

  /**
   * Creates json adhering to the judges specification for an EmergentTarget
   *
   * @return a JsonNode object
   */
  public JsonNode toJson() {
    ObjectNode rootNode = new ObjectMapper().createObjectNode();

    rootNode.put("type", "emergent");
    if (this.getGeotag() != null) {
      if (this.getGeotag().getGpsLocation() != null
          && (Double) this.getGeotag().getGpsLocation().getLatitude() != null
          && !((Double) this.getGeotag().getGpsLocation().getLatitude()).isNaN()) {
        rootNode.put("latitude", this.getGeotag().getGpsLocation().getLatitude());
      }
      if (this.getGeotag().getGpsLocation() != null
          && (Double) this.getGeotag().getGpsLocation().getLongitude() != null
          && !((Double) this.getGeotag().getGpsLocation().getLongitude()).isNaN()) {
        rootNode.put("longitude", this.getGeotag().getGpsLocation().getLongitude());
      }
    }
    rootNode.put("description", this.description);
    rootNode.put("autonomous", this.getCreator().getUserType() == ODLCUser.UserType.ADLC);
    return rootNode;
  }

}
