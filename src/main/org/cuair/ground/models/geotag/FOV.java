package org.cuair.ground.models.geotag;

import javax.persistence.Entity;
import org.cuair.ground.models.CUAirModel;

@Entity
public class FOV extends CUAirModel {
  private double x;
  private double y;

  public FOV(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }
}
