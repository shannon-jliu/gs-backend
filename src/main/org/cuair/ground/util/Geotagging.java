package org.cuair.ground.util;

import java.util.Arrays;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;
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
  /** Field of view of camera horizontally and vertically */
  private static double FOV_HORIZONTAL_RADIANS = Flags.FOV_HORIZONTAL_RADIANS;
  private static double FOV_VERTICAL_RADIANS = Flags.FOV_VERTICAL_RADIANS;
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
   * @param latitude        The latitude of the plane
   * @param longitude       The longitude of the plane
   * @param altitude        The altitude of the plane
   * @param pixelx          The x-coordinate of the pixel center of the tag on the frontend with respect to the image
   * @param pixely          The y-coordinate of the pixel center of the tag on the frontend with respect to the image
   * @param planeYawRadians The yaw of the plane in radians
   */
  public static GpsLocation getPixelCoordinates(
      double latitude,
      double longitude,
      double altitude,
      double pixelx,
      double pixely,
      double planeYawRadians) {
    // total horizontal distance imaged in meters
    double hdi =
        2
            * altitude
            * Math.tan(
            FOV_HORIZONTAL_RADIANS
                / 2); // telemetryData.getAerialPosition().getAltitudeGroundFt()
    double vdi =
        2
            * altitude
            * Math.tan(
            FOV_VERTICAL_RADIANS
                / 2); // telemetryData.getAerialPosition().getAltitudeGroundFt()

    // distance covered per pixel in meters/pixel
    double dpphoriz = hdi / IMAGE_WIDTH;
    double dppvert = vdi / IMAGE_HEIGHT;

    // finding distance from the center
    double deltapixel_x = pixelx - (IMAGE_WIDTH / 2);
    double deltapixel_y = (IMAGE_HEIGHT / 2) - pixely;

    double dppH = deltapixel_x * dpphoriz;
    double dppV = deltapixel_y * dppvert;

    // matrix rotation to account for the yaw - (clockwise)
    double target_reference_x_meters =
        dppH * Math.cos(planeYawRadians) + dppV * Math.sin(planeYawRadians);
    double target_reference_y_meters =
        dppH * -1 * Math.sin(planeYawRadians) + dppV * Math.cos(planeYawRadians);

    // find the change from the plane's center in longitude and latitude
    double deltaLong =
        haversineXMetersToLongitude(target_reference_x_meters, latitude) * 180 / Math.PI;
    double deltaLat = haversineYMetersToLatitude(target_reference_y_meters) * 180 / Math.PI;

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
            .filter(g -> g != null)
            .filter(g -> g.getGpsLocation() != null)
            .filter(g -> g.getClockwiseRadiansFromNorth() != null)
            .toArray(Geotag[]::new);
    if (geotags == null || geotags.length == 0) {
      return null;
    }
    GpsLocation[] locations =
        Arrays.stream(geotags).map(Geotag::getGpsLocation).toArray(GpsLocation[]::new);
    Double[] radians =
        Arrays.stream(geotags).map(Geotag::getClockwiseRadiansFromNorth).toArray(Double[]::new);
    return new Geotag(GpsLocation.median(locations), Radian.median(radians));
  }
}
