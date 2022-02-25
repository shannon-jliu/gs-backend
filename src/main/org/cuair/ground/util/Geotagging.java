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
  public static double IMAGE_WIDTH = Flags.IMAGE_WIDTH;
  public static double IMAGE_HEIGHT = Flags.IMAGE_HEIGHT;
  /** An approximation of the radius of the Earth in meters */
  private static double radiusEarth = 6371000.0;

  /**
   * Returns the change in longitude (in radians) equivalent to a given change in meters in the x (East/West) direction
   * at a given latitude, assuming the change in latitude is 0
   *
   * @param metersX  The change in meters in the x (East/West) direction
   * @param latitude The latitude in radians
   */
  private static double haversineXMetersToLongitude(double metersX, double latitude) {
    double sinSquaredTerm = 2 * Math.pow(Math.sin(metersX / (2 * radiusEarth)), 2);
    double cosSquaredTerm = Math.pow(Math.cos(latitude), 2);
    return Math.acos(1 - (sinSquaredTerm / cosSquaredTerm));
  }

  /**
   * Returns the change in latitude (in radians) equivalent to a given change in meters in the y (North/South) direction,
   * assuming the change in longitude is 0
   *
   * @param metersY The change in meters in the y (North/South) direction
   */
  private static double haversineYMetersToLatitude(double metersY) {
    double sinSquaredTerm = 2 * Math.pow(Math.sin(metersY / (2 * radiusEarth)), 2);
    return Math.acos(1 - sinSquaredTerm);
  }

  /**
   * Creates a Gpslocation representing the center of the image
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

    // total horizontal distance imaged in meters
    double hdi =
        2
            * altitude
            * Math.tan(
            fovHoriz
                / 2);

    logger.info("hdi: " + hdi);

    // total vertical distance imaged in meters
    double vdi =
        2
            * altitude
            * Math.tan(
            fovVert
                / 2);

    logger.info("vdi: " + vdi);

    // distance covered per pixel in meters/pixel
    double dpphoriz = hdi / IMAGE_WIDTH;
    double dppvert = vdi / IMAGE_HEIGHT;

    // finding distance from the center
    double deltapixel_x = pixelx - (IMAGE_WIDTH / 2);
    double deltapixel_y = (IMAGE_HEIGHT / 2) - pixely;

    logger.info("dpx: " + deltapixel_x);
    logger.info("dpy: " + deltapixel_y);

    double dppH = deltapixel_x * dpphoriz;
    double dppV = deltapixel_y * dppvert;

    // Do rotation of coordinate system to rotate dppH and dppV to account for yaw
    double target_reference_x_meters =
        dppH * Math.cos(planeYawRadians) + dppV * Math.sin(planeYawRadians);
    double target_reference_y_meters =
        dppH * -1 * Math.sin(planeYawRadians) + dppV * Math.cos(planeYawRadians);

    logger.info("target_ref_x: " + target_reference_x_meters);
    logger.info("target_ref_y: " + target_reference_y_meters);

    // find the change from the plane's center in longitude and latitude
    double latRadians = Math.PI / 180 * latitude; // convert to latitude in radians for haversine
    double deltaLong = target_reference_x_meters / (111111*Math.cos(Math.sqrt(2)/2));
        // haversineXMetersToLongitude(target_reference_x_meters, latRadians) * 180 / Math.PI;
    double deltaLat = target_reference_y_meters / 111111;
        //haversineYMetersToLatitude(target_reference_y_meters) * 180 / Math.PI;

    logger.info("deltaLong: " + deltaLong);
    logger.info("deltaLat: " + deltaLat);

    // adding the distance from the center to the plane's center position
    double longitude_of_target_x = longitude + deltaLong;
    double latitude_of_target_y = latitude + deltaLat;

    GpsLocation gps = null;
    try {
      gps = new GpsLocation(latitude_of_target_y, longitude_of_target_x);
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
