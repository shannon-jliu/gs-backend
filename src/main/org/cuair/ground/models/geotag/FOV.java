package org.cuair.ground.models.geotag;

import javax.persistence.Entity;
import org.cuair.ground.models.CUAirModel;
import org.cuair.ground.util.Flags;

/** Represents the field of view (FOV) of the camera during image capture in radians.  */

@Entity
public class FOV extends CUAirModel {
  private double x;
  private double y;

  public FOV(double x, double y) {
    this.x = x;
    this.y = y;
  }

  // Expects focalLength in mm, returns an FOV in radians
  public static FOV fromFocalLength(double focalLength) {
    double CAM_SENSOR_WIDTH = Flags.CAM_SENSOR_WIDTH;
    double CAM_SENSOR_HEIGHT = Flags.CAM_SENSOR_HEIGHT;

    double fovHoriz = 2 * Math.atan(CAM_SENSOR_WIDTH / (2 * focalLength));
    double fovVert = 2 * Math.atan(CAM_SENSOR_HEIGHT / (2 * focalLength));

    return new FOV(fovHoriz, fovVert);
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }
}
