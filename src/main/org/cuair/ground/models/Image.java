package org.cuair.ground.models;

import java.util.HashMap;
import java.util.Map;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.util.Geotagging;

public class Image {

  /** The URL where clients can retrieve the image file  */
  private String imageUrl;

  /** Closest Telemetry for when this Image was taken  */
  private Telemetry telemetry;

  /** The type of this image. Either FIXED, TRACKING, or OFFAXIS */
  private ImgMode imgMode;

  /** True if has at least one associated MDLC assignment, otherwise false. */
  private boolean hasMdlcAssignment = false;

  /** True if has at least one associated ADLC assignment, otherwise false. */
  private boolean hasAdlcAssignment = false;

  /** The field of view of this image in degrees (horizontal, vertical). */
  private double[] fov;

  /** The possible image modes: fixed, tracking, and off-axis */
  private enum ImgMode {
    FIXED,
    TRACKING,
    OFFAXIS
  }

  public Image (String imageUrl, Telemetry telemetry, double[] fov, ImgMode imgMode) {
    this.imageUrl = imageUrl;
    this.telemetry = telemetry;
    this.fov = fov;
    this.imgMode = imgMode;
  }

  /**
   * Internal method for finding geotags corresponding to four corners of image
   */
  public Map<String, GpsLocation> getLocations() {
    GpsLocation imageGPS = telemetry.getGps();
    double centerLat = imageGPS.getLatitude();
    double centerLong = imageGPS.getLongitude();

    double planeYaw = telemetry.getPlaneYaw();
    double altitude = telemetry.getAltitude();

    GpsLocation topLeft = Geotagging
        .getPixelCoordinates(centerLat, centerLong, altitude, fov, 0.0, 0.0, planeYaw);
    GpsLocation topRight = Geotagging
        .getPixelCoordinates(centerLat, centerLong, altitude, fov, Geotagging.IMAGE_WIDTH, 0.0, planeYaw);
    GpsLocation bottomLeft = Geotagging
        .getPixelCoordinates(centerLat, centerLong, altitude, fov, 0.0, Geotagging.IMAGE_HEIGHT, planeYaw);
    GpsLocation bottomRight = Geotagging
        .getPixelCoordinates(centerLat, centerLong, altitude, fov, Geotagging.IMAGE_WIDTH, Geotagging.IMAGE_HEIGHT, planeYaw);

    Map<String, GpsLocation> locs = new HashMap<String, GpsLocation>();
    locs.put("topLeft", topLeft);
    locs.put("topRight", topRight);
    locs.put("bottomLeft", bottomLeft);
    locs.put("bottomRight", bottomRight);

    return locs;
  }

}
