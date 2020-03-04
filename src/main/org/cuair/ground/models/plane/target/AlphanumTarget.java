package org.cuair.ground.models.plane.target;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import javax.persistence.Entity;
import org.cuair.ground.models.Color;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.Shape;
import org.cuair.ground.models.geotag.CardinalDirection;
import org.cuair.ground.models.geotag.Geotag;

/** Alphanum Target is target that is associated with Alphanumeric Target Sightings. */
@Entity
public class AlphanumTarget extends Target {

  /** A description of the shape of the target */
  private Shape shape;

  /** The color of the shape */
  private Color shapeColor;

  /**
   * The alphanumeric of this target. It is a String and not a char because targets in the past have
   * had more than one alphanumeric on them
   */
  private String alpha;

  /** The color of the alphanumeric */
  private Color alphaColor;

  /** True if the target is the off axis target, false otherwise */
  private Boolean offaxis;

  /**
   * Creates an AlphanumTarget
   *
   * @param creator        the Creator of the Target
   * @param shape          String description of the shape of the Target
   * @param shapeColor     String color of the shape
   * @param alpha          String alphanumeric of this Target
   * @param alphaColor     String color of the alphanumeric
   * @param offaxis        Boolean of whether the target is off-axis
   * @param geotag         Geotag of this Target
   * @param judgeTargetId  Long id of this Target on the competition server
   * @param thumbnailTsid Long id of Target Sighting used for thumbnail
   */
  public AlphanumTarget(
      ODLCUser creator,
      Shape shape,
      Color shapeColor,
      String alpha,
      Color alphaColor,
      Boolean offaxis,
      Geotag geotag,
      Long judgeTargetId,
      Long thumbnailTsid) {
    super(creator, geotag, judgeTargetId, thumbnailTsid);
    this.shape = shape;
    this.shapeColor = shapeColor;
    this.alpha = alpha;
    this.alphaColor = alphaColor;
    this.offaxis = offaxis;
  }

  /**
   * Given another target, it updates all fields of this instance if there are any differences
   *
   * @param other Target containing updated fields
   */
  @Override
  public void updateFromTarget(Target other) {
    super.updateFromTarget(other);
    AlphanumTarget alphaTarget = null;

    if (!(other instanceof AlphanumTarget)) {
      return;
    }
    alphaTarget = (AlphanumTarget) other;
    if (alphaTarget.getShape() != null) {
      this.shape = alphaTarget.getShape();
    }
    if (alphaTarget.getShapeColor() != null) {
      this.shapeColor = alphaTarget.getShapeColor();
    }
    if (alphaTarget.getAlpha() != null) {
      this.alpha = alphaTarget.getAlpha();
    }
    if (alphaTarget.getAlphaColor() != null) {
      this.alphaColor = alphaTarget.getAlphaColor();
    }
    if (alphaTarget.isOffaxis() != null) {
      this.offaxis = alphaTarget.isOffaxis();
    }
    if (alphaTarget.getGeotag() != null) {
      this.geotag = alphaTarget.getGeotag();
    }
  }

  /** Returns class associated with this target */
  @Override
  public Class<? extends TargetSighting> fetchAssociatedTargetSightingClass() {
    return AlphanumTargetSighting.class;
  }

  /**
   * Gets the description of the shape of this Target
   *
   * @return String description of the shape
   */
  public Shape getShape() {
    return shape;
  }

  /**
   * Sets the description of the shape of this Target
   *
   * @param shape String new description of the shape
   */
  public void setShape(Shape shape) {
    this.shape = shape;
  }

  /**
   * Gets the color of the shape
   *
   * @return String color of the shape
   */
  public Color getShapeColor() {
    return shapeColor;
  }

  /**
   * Sets the color of the shape
   *
   * @param shapeColor String new color of the shape
   */
  public void setShapeColor(Color shapeColor) {
    this.shapeColor = shapeColor;
  }

  /**
   * Gets the alphanumeric of the Target
   *
   * @return String alphanumeric
   */
  public String getAlpha() {
    return alpha;
  }

  /**
   * Sets the alphanumeric of the Target
   *
   * @param alpha String new alphanumeric of the Target
   */
  public void setAlpha(String alpha) {
    this.alpha = alpha;
  }

  /**
   * Gets the color of the alphanumeric
   *
   * @return String color of the alphanumeric
   */
  public Color getAlphaColor() {
    return alphaColor;
  }

  /**
   * Sets the color of the alphanumeric
   *
   * @param alphaColor String new color of the alphanumeric
   */
  public void setAlphaColor(Color alphaColor) {
    this.alphaColor = alphaColor;
  }

  /**
   * Gets whether target is offaxis
   *
   * @return Boolean true if target is offaxis, false otherwise
   */
  public Boolean isOffaxis() {
    return offaxis;
  }

  /**
   * Sets whether target is offaxis
   *
   * @param offaxis Boolean true if target is offaxis, false otherwise
   */
  public void setOffaxis(Boolean offaxis) {
    this.offaxis = offaxis;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !super.equals(o)) {
      return false;
    }

    AlphanumTarget other = (AlphanumTarget) o;

    if (!Objects.deepEquals(this.shape, other.getShape())) {
      return false;
    }

    if (!Objects.deepEquals(this.shapeColor, other.getShapeColor())) {
      return false;
    }

    if (!Objects.deepEquals(this.alpha, other.getAlpha())) {
      return false;
    }

    if (!Objects.deepEquals(this.alphaColor, other.getAlphaColor())) {
      return false;
    }

    return Objects.deepEquals(this.offaxis, other.isOffaxis());
  }

  @Override
  @JsonIgnore
  public String getTypeString() {
    return "Alphanum";
  }

  /**
   * Creates json adhering to the judges specification for an AlphanumTarget
   *
   * @return a JsonNode object
   */
  public JsonNode toJson() {

    ObjectNode rootNode = new ObjectMapper().createObjectNode();

    if (offaxis) {
      rootNode.put("type", "off_axis");
    } else {
      rootNode.put("type", "standard");
    }

    if (this.getGeotag() != null && !this.isOffaxis()) {
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
      if (this.getGeotag().getRadiansFromNorth() != null) {
        rootNode.put(
            "orientation",
            CardinalDirection.getFromRadians(this.getGeotag().getRadiansFromNorth())
                .getAbbreviation());
      }
    }
    if (this.shape != null) {
      rootNode.put("shape", this.shape.getName());
    }

    rootNode.put("alphanumeric", this.alpha);

    if (this.shapeColor != null) {
      rootNode.put("background_color", this.shapeColor.name().toLowerCase());
    }

    if (this.alphaColor != null) {
      rootNode.put("alphanumeric_color", this.alphaColor.name().toLowerCase());
    }
    rootNode.put("autonomous", this.getCreator().getUserType() == ODLCUser.UserType.ADLC);

    return rootNode;
  }
}
