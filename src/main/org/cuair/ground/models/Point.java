package org.cuair.ground.models;

import javax.persistence.Entity;
import com.fasterxml.jackson.annotation.JsonValue;
import org.cuair.ground.models.CUAirModel;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;

/** Represents the different colors on the Target */
@Entity
public class Point extends CUAirModel{

  @ManyToOne(cascade = {CascadeType.MERGE})
  protected AlphanumTargetSighting targetSightng;  

  private Integer x;
  private Integer y;

  /**
   * Creates a Point
   */
  public Point(Integer x, Integer y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Gets the x coordinate
   * @return the x coordinate
   */
  // @JsonValue
  public Integer getX() {
    return x;
  }

  /**
   * Gets the y coordinate
   * @return the y coordinate
   */
  // @JsonValue
  public Integer getY() {
    return y;
  }
}
