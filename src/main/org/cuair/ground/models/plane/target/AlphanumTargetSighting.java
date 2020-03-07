package org.cuair.ground.models.plane.target;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.Color;
import org.cuair.ground.models.Confidence;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.Shape;
import org.cuair.ground.models.geotag.Geotag;

/** Alphanumeric Target Sighting that has features associated with alphanmuerics. */
@Entity
public class AlphanumTargetSighting extends TargetSighting {

  /** The target of this target sighting */
  @ManyToOne
  protected AlphanumTarget target;

  /** A description of the shape of the target */
  private Shape shape;

  /** The confidence the vision system has in the shape identification */
  private Double shapeConfidence;

  /** The color of the shape */
  private Color shapeColor;

  /** The confidence the vision system has in the shape color identification */
  private Double shapeColorConfidence;

  /** The alphanumeric of this target sighting */
  private String alpha;

  /** The confidence the vision system has in the alphanumeric identification */
  private Double alphaConfidence;

  /** The color of the alphanumeric */
  private Color alphaColor;

  /** The confidence the vision system has in the alphanumeric color identification */
  private Double alphaColorConfidence;

  /** The overall confidence the vision system has in classification */
  @JsonIgnore
  private Double adlcClassConf;

  /** True if the target sighting is of the off axis target, false otherwise */
  private Boolean offaxis;

  /**
   * Creates an Alphanumeric Target Sighting
   *
   * @param creator        the ODLCUser that created this Target Sighting
   * @param shape          String description of the shape of the Target Sighting
   * @param shapeColor     String color of the shape
   * @param alpha          String alphanumeric of this Target Sighting
   * @param alphaColor     String color of the alphanumeric
   * @param offaxis        Boolean of whether the target is off-axis
   * @param geotag         Geotag of this Target Sighting
   * @param pixelX         Integer x pixel coordinate of the center of the target sighting in the specific
   *                       Image
   * @param pixelY         Integer y pixel coordinate of the center of the target sighting in the specific
   *                       Image
   * @param target         the Target of this Target Sighting
   * @param radiansFromTop the orientation of the Target Sighting
   * @param assignment     the assignment that created this TargetSighting
   * @param width          the horizontal pixel width of the TargetSighting
   * @param height         the vertical pixel height of the TargetSighting
   */
  @JsonCreator
  public AlphanumTargetSighting(
      @JsonProperty("creator") ODLCUser creator,
      @JsonProperty("shape") Shape shape,
      @JsonProperty("shapeColor") Color shapeColor,
      @JsonProperty("alpha") String alpha,
      @JsonProperty("alphaColor") Color alphaColor,
      @JsonProperty("offaxis") Boolean offaxis,
      @JsonProperty("pixelX") Integer pixelX,
      @JsonProperty("pixelY") Integer pixelY,
      @JsonProperty("width") Integer width,
      @JsonProperty("height") Integer height,
      @JsonProperty("geotag") Geotag geotag,
      @JsonProperty("target") AlphanumTarget target,
      @JsonProperty("radiansFromTop") Double radiansFromTop,
      @JsonProperty("assignment") Assignment assignment,
      @JsonProperty("shapeConfidence") Double shapeConfidence,
      @JsonProperty("shapeColorConfidence") Double shapeColorConfidence,
      @JsonProperty("alphaConfidence") Double alphaConfidence,
      @JsonProperty("alphaColorConfidence") Double alphaColorConfidence,
      @JsonProperty("orientationConfidence") Double orientationConfidence,
      @JsonProperty("mdlcClassConf") Confidence mdlcClassConf) {
    super(
        creator,
        pixelX,
        pixelY,
        width,
        height,
        geotag,
        radiansFromTop,
        orientationConfidence,
        mdlcClassConf,
        assignment);
    this.shape = shape;
    this.shapeColor = shapeColor;
    this.alpha = alpha;
    this.alphaColor = alphaColor;
    this.target = target;
    this.shapeConfidence = shapeConfidence;
    this.shapeColorConfidence = shapeColorConfidence;
    this.alphaConfidence = alphaConfidence;
    this.alphaColorConfidence = alphaColorConfidence;
    this.offaxis = offaxis;

    updateAdlcClassConf();
  }

  /**
   * Gets the target
   *
   * @return Target
   */
  @Override
  public AlphanumTarget getTarget() {
    return target;
  }

  /**
   * Sets the target
   *
   * @param target new Target
   */
  public void setTarget(AlphanumTarget target) {
    this.target = target;
  }

  @Override
  public void makeAssociatedTargetNull() {
    this.target = null;
  }

  /**
   * Given another target sighting, it updates all fields of this instance if there are any
   * differences
   *
   * @param other AlphanumTargetSighting containing updated fields
   */
  public void updateFromTargetSighting(TargetSighting other) {
    boolean updateConf =
        other.getOrientationConfidence() != null
            && !Objects.deepEquals(
            other.getOrientationConfidence(), this.getOrientationConfidence());
    super.updateFromTargetSighting(other);
    AlphanumTargetSighting alphaSighting;

    assert other instanceof AlphanumTargetSighting;

    alphaSighting = (AlphanumTargetSighting) other;

    if (alphaSighting.getShape() != null) {
      this.shape = alphaSighting.getShape();
    }
    if (alphaSighting.getShapeColor() != null) {
      this.shapeColor = alphaSighting.getShapeColor();
    }
    if (alphaSighting.getAlpha() != null) {
      this.alpha = alphaSighting.getAlpha();
    }
    if (alphaSighting.getAlphaColor() != null) {
      this.alphaColor = alphaSighting.getAlphaColor();
    }
    if (alphaSighting.isOffaxis() != null) {
      this.offaxis = alphaSighting.isOffaxis();
    }
    if (alphaSighting.getShapeConfidence() != null) {
      if (!updateConf && !alphaSighting.getShapeConfidence().equals(this.shapeConfidence)) {
        updateConf = true;
      }
      this.shapeConfidence = alphaSighting.getShapeConfidence();
    }
    if (alphaSighting.getShapeColorConfidence() != null) {
      if (!updateConf
          && !alphaSighting.getShapeColorConfidence().equals(this.shapeColorConfidence)) {
        updateConf = true;
      }
      this.shapeColorConfidence = alphaSighting.getShapeColorConfidence();
    }
    if (alphaSighting.getAlphaConfidence() != null) {
      if (!updateConf && !alphaSighting.getAlphaConfidence().equals(this.alphaConfidence)) {
        updateConf = true;
      }
      this.alphaConfidence = alphaSighting.getAlphaConfidence();
    }
    if (alphaSighting.getAlphaColorConfidence() != null) {
      if (!updateConf
          && !alphaSighting.getAlphaColorConfidence().equals(this.alphaColorConfidence)) {
        updateConf = true;
      }
      this.alphaColorConfidence = alphaSighting.getAlphaColorConfidence();
    }

    if (updateConf) {
      updateAdlcClassConf();
    }

    this.target = alphaSighting.getTarget();
  }

  /**
   * Updates {@code adlcClassConf} to be the product of the maximum 3 confidence values if the
   * sighting is ADLC and 2 or fewer confidence values are null. Otherwise, sets {@code
   * adlcClassConf} to null.
   */
  private void updateAdlcClassConf() {
    Double[] sortedConf =
        new Double[] {
            alphaConfidence,
            alphaColorConfidence,
            shapeConfidence,
            shapeColorConfidence,
            getOrientationConfidence()
        };

    // Double comparator where null is always lowest
    Comparator<Double> confCmp =
        (Double o1, Double o2) -> {
          if (o1 == null) {
            return -1;
          }
          if (o2 == null) {
            return 1;
          }
          return o1.compareTo(o2);
        };

    Arrays.sort(sortedConf, confCmp);
    if (sortedConf[4] == null || sortedConf[3] == null || sortedConf[2] == null) {
      adlcClassConf = null;
    } else {
      adlcClassConf = sortedConf[4] * sortedConf[3] * sortedConf[2];
    }
  }

  /**
   * Gets the shape
   *
   * @return Shape description of the shape
   */
  public Shape getShape() {
    return shape;
  }

  /**
   * Sets the shape
   *
   * @param shape Shape description of the shape
   */
  public void setShape(Shape shape) {
    this.shape = shape;
  }

  /**
   * Gets the shape color
   *
   * @return Color of the shape
   */
  public Color getShapeColor() {
    return shapeColor;
  }

  /**
   * Sets the color of the shape
   *
   * @param shapeColor Color of the shape
   */
  public void setShapeColor(Color shapeColor) {
    this.shapeColor = shapeColor;
  }

  /**
   * Gets the alphanumeric
   *
   * @return String alphanumeric
   */
  public String getAlpha() {
    return alpha;
  }

  /**
   * Sets the alphanumeric
   *
   * @param alpha String alphanumeric
   */
  public void setAlpha(String alpha) {
    this.alpha = alpha;
  }

  /**
   * Gets the color of the alphanumeric
   *
   * @return Color of the alphanumeric
   */
  public Color getAlphaColor() {
    return alphaColor;
  }

  /**
   * Sets the color of the alphanumeric
   *
   * @param alphaColor Color of the alphanumeric
   */
  public void setAlphaColor(Color alphaColor) {
    this.alphaColor = alphaColor;
  }

  /**
   * Gets whether sighting is offaxis
   *
   * @return Boolean true if sighting is offaxis, false otherwise
   */
  public Boolean isOffaxis() {
    return offaxis;
  }

  /**
   * Sets whether sighting is offaxis
   *
   * @param offaxis Boolean true if sighting is offaxis, false otherwise
   */
  public void setOffaxis(Boolean offaxis) {
    this.offaxis = offaxis;
  }

  /**
   * Gets the confidence the vision system has in the shape classification
   *
   * @return Double representing the shape confidence
   */
  public Double getShapeConfidence() {
    return shapeConfidence;
  }

  /**
   * Sets the confidence the vision system has in the shape classification
   *
   * @param shapeConfidence Double representing the shape confidence
   */
  public void setShapeConfidence(Double shapeConfidence) {
    this.shapeConfidence = shapeConfidence;
    updateAdlcClassConf();
  }

  /**
   * Gets the confidence the vision system has in the shape color classification
   *
   * @return Double representing the shape color confidence
   */
  public Double getShapeColorConfidence() {
    return shapeColorConfidence;
  }

  /**
   * Sets the confidence the vision system has in the shape color classification
   *
   * @param shapeColorConfidence Double representing the shape color confidence
   */
  public void setShapeColorConfidence(Double shapeColorConfidence) {
    this.shapeColorConfidence = shapeColorConfidence;
    updateAdlcClassConf();
  }

  /**
   * Gets the confidence the vision system has in the alphanumeric classification
   *
   * @return Double representing the alphanumeric confidence
   */
  public Double getAlphaConfidence() {
    return alphaConfidence;
  }

  /**
   * Sets the confidence the vision system has in the alphanumeric classification
   *
   * @param alphaConfidence Double representing the alphanumeric confidence
   */
  public void setAlphaConfidence(Double alphaConfidence) {
    this.alphaConfidence = alphaConfidence;
    updateAdlcClassConf();
  }

  /**
   * Gets the confidence the vision system has in the alphanumeric color classification
   *
   * @return Double representing the alphanumeric color confidence
   */
  public Double getAlphaColorConfidence() {
    return alphaColorConfidence;
  }

  @Override
  public void setOrientationConfidence(Double orientationConfidence) {
    super.setOrientationConfidence(orientationConfidence);
    updateAdlcClassConf();
  }

  /**
   * Determines if the given object is logically equal to this AlphanumTargetSighting
   *
   * @param o The object to compare
   * @return True if the object equals this AlphanumTargetSighting
   */
  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }

    AlphanumTargetSighting other = (AlphanumTargetSighting) o;

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

    if (!Objects.deepEquals(this.offaxis, other.isOffaxis())) {
      return false;
    }

    if (!Objects.deepEquals(this.target, other.getTarget())) {
      return false;
    }

    if (!Objects.deepEquals(this.shapeConfidence, other.getShapeConfidence())) {
      return false;
    }

    if (!Objects.deepEquals(this.shapeColorConfidence, other.getShapeColorConfidence())) {
      return false;
    }

    if (!Objects.deepEquals(this.alphaConfidence, other.getAlphaConfidence())) {
      return false;
    }

    return Objects.deepEquals(this.alphaColorConfidence, other.getAlphaColorConfidence());
  }
}
