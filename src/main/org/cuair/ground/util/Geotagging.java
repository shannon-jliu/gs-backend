package org.cuair.ground.util;

import java.util.Arrays;
import java.util.Objects;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;
import org.cuair.ground.models.geotag.FOV;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Radian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geotagging {
  private static final Logger logger = LoggerFactory.getLogger(GpsLocation.class);
  /** Width of height and image in pixels */
  public static double IMAGE_WIDTH = Flags.FRONTEND_IMAGE_WIDTH;
  public static double IMAGE_HEIGHT = Flags.FRONTEND_IMAGE_HEIGHT;
  /** An approximation of the radius of the Earth in meters */
  private static double radiusEarth = 6371000.0;

  /**
   * Uses the inverse haversine function to return new gps corresponding to a translation
   * of given distance and direction from an initial gps reading.
   * @param initLat         The initial latitude
   * @param initLong        The initial longitude
   * @param distance        The distance offset travelled (in meters)
   * @param direction       The direction travelled (in radians clockwise from north)
   * @return an array of two doubles, [latitude, longitude] (in degrees)
   */
  private static double[] inverseHaversine(double initLat, double initLong, double distance, double direction) {
    double r = radiusEarth;

    // Initialize empty gps array
    double[] gps = new double[2];

    // New latitude
    gps[0] = Math.asin(Math.sin(initLat) * Math.cos(distance / r)
              + Math.cos(initLat) * Math.sin(distance / r) * Math.cos(direction));

    // New longitude
    gps[1] = initLong + Math.atan2(Math.sin(direction) * Math.sin(distance / r) * Math.cos(initLat),
        Math.cos(distance / r) - Math.sin(initLat) * Math.sin(gps[0]));

    // Convert into degrees
    gps[0] = gps[0] * 180 / Math.PI;
    gps[1] = gps[1] * 180 / Math.PI;
    return gps;
  }


  /**
   * Creates a GpsLocation representing the center of the image
   *
   * @param latitude        The latitude of the plane in degrees
   * @param longitude       The longitude of the plane in degrees
   * @param altitude        The altitude of the plane in meters
   * @param fov             The (horizontal, vertical) fov of the camera
   * @param pixelx          The x-coordinate of the pixel center of the tag on the frontend with respect to the image
   * @param pixely          The y-coordinate of the pixel center of the tag on the frontend with respect to the image
   * @param planeYawRadians The yaw of the plane in radians
   */
  public static GpsLocation getPixelCoordinates(
      double latitude,
      double longitude,
      double altitude,
      FOV fov,
      double pixelx,
      double pixely,
      double planeYawRadians) {

    double fovHoriz = fov.getX();
    double fovVert = fov.getY();

    // total horizontal (x) distance imaged in meters
    double hdi =
        2
            * altitude
            * Math.tan(
            fovHoriz
                / 2);

    logger.info("hdi: " + hdi);

    // total vertical (y) distance imaged in meters
    double vdi =
        2
            * altitude
            * Math.tan(
            fovVert
                / 2);

    logger.info("vdi: " + vdi);

    // Distance covered per pixel in meters/pixel
    double dpphoriz = hdi / IMAGE_WIDTH;
    double dppvert = vdi / IMAGE_HEIGHT;

    // Find pixel offset from the center
    double deltapixel_x = pixelx - (IMAGE_WIDTH / 2);
    double deltapixel_y = (IMAGE_HEIGHT / 2) - pixely;

    logger.info("dpx: " + deltapixel_x);
    logger.info("dpy: " + deltapixel_y);

    // Find horizontal and vertical physical distance from center with respect to image
    double dppH = deltapixel_x * dpphoriz;
    double dppV = deltapixel_y * dppvert;

    // Do rotation of coordinate system to rotate dppH and dppV to account for yaw
    double target_dx = dppH * Math.cos(planeYawRadians) + dppV * Math.sin(planeYawRadians);
    double target_dy = dppH * -1 * Math.sin(planeYawRadians) + dppV * Math.cos(planeYawRadians);

    // Compute new gps using inverse haversine
    double latRadians = Math.PI / 180 * latitude;
    double longRadians = Math.PI / 180 * longitude;
    double distance = Math.sqrt(Math.pow(target_dx, 2) + Math.pow(target_dy, 2));
    double direction = planeYawRadians + (Math.PI / 2.0 - Math.atan2(target_dy, target_dx));
    double[] newGps = inverseHaversine(latRadians, longRadians, distance, direction);

    System.out.println(newGps[0]);
    System.out.println(newGps[1]);

    GpsLocation gps = null;
    try {
      gps = new GpsLocation(newGps[0], newGps[1]);
    } catch (InvalidGpsLocationException e) {
      logger.error(e.getMessage());
    }
    return gps;
  }

  /**
   * Calculate the orientation of this geotag as radians from north
   *
   * @param planeYaw       The yaw of the plane in radians, going clockwise from 0 = north
   * @param radiansFromTop The radians from the top of the image
   * @return The orientation of this geotag
   */
  public static Double calculateClockwiseRadiansFromNorth(double planeYaw, double radiansFromTop) {
    return Radian.normalize(planeYaw + radiansFromTop);
  }

  /**
   * Median a variable number of geotags
   *
   * @param geotags The geotag objects to medianed
   * @return The medianed geotag object
   */
  public static Geotag median(Geotag... geotags) {
    geotags =
        Arrays.stream(geotags)
            .filter(Objects::nonNull)
            .filter(g -> g.getGpsLocation() != null)
            .filter(g -> g.getClockwiseRadiansFromNorth() != null)
            .toArray(Geotag[]::new);
    if (geotags.length == 0) {
      return null;
    }
    GpsLocation[] locations =
        Arrays.stream(geotags).map(Geotag::getGpsLocation).toArray(GpsLocation[]::new);
    Double[] radians =
        Arrays.stream(geotags).map(Geotag::getClockwiseRadiansFromNorth).toArray(Double[]::new);
    return new Geotag(GpsLocation.median(locations), Radian.median(radians));
  }
}
