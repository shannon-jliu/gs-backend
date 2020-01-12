package org.cuair.ground.models.geotag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
// import org.cuair.ground.util.PlayConfig;
import org.jetbrains.annotations.Nullable;
// import play.Logger;

/** Represents a GPS location in the world */
// TODO: Figure out if this annotation is necessary
// @Embeddable
public class GpsLocation {

  /** Maximum valid latitude */
  public static final Double ABS_LATITUDE_BOUND = 90.0;
  /** Maximum valid longitude */
  public static final Double ABS_LONGITUDE_BOUND = 180.0;

  /** The weight of the manually created geotags for weighted average */
  private static final double MANUAL_GEOTAG_WEIGHT = 0.0;

  /** The weight of the automatically created geotags for weighted average */
  private static final double AUTO_GEOTAG_WEIGHT = 1 - 0.0;

  /** The latitude of the GPS location */
  @Basic(optional = true)
  private Double latitude;
  /** The longitude of the GPS location */
  @Basic(optional = true)
  private Double longitude;

  /**
   * Creates a new GPS location with the given latitude and longitude
   *
   * @param latitude The latitude of the GPS location
   * @param longitude The longitude of the GPS location
   * @throws InvalidGpsLocationException If latitude is not in range [-90.0,90.0]
   * @throws InvalidGpsLocationException If longitude is not in range [-180.0,180.0]
   */
  public GpsLocation(Double latitude, Double longitude) throws InvalidGpsLocationException {

    if (Math.abs(latitude) > ABS_LATITUDE_BOUND) {
      throw new InvalidGpsLocationException(
          "Latitude should be within -" + ABS_LATITUDE_BOUND + " and " + ABS_LATITUDE_BOUND);
    }
    if (Math.abs(longitude) > ABS_LONGITUDE_BOUND) {
      throw new InvalidGpsLocationException(
          "Longitude should be within"
              + " -"
              + ABS_LONGITUDE_BOUND
              + " and "
              + ABS_LONGITUDE_BOUND);
    }
    this.latitude = latitude;
    this.longitude = longitude;
  }

  /**
   * Get the latitude of this GPS location
   *
   * @return The latitude of this GPS location
   */
  public Double getLatitude() {
    return latitude;
  }

  /**
   * Get the longitude of this GPS location
   *
   * @return The longitude of this GPS location
   */
  public Double getLongitude() {
    return longitude;
  }

  /**
   * Change the latitude of this GPS location
   *
   * @param latitude The new latitude for this GPS location
   * @throws InvalidGpsLocationException If latitude is not in range [-90.0,90.0]
   */
  public void setLatitude(Double latitude) throws InvalidGpsLocationException {
    if (latitude != null && Math.abs(latitude) > ABS_LATITUDE_BOUND) {
      throw new InvalidGpsLocationException(
          "Latitude should be within -" + ABS_LATITUDE_BOUND + " and " + ABS_LATITUDE_BOUND);
    }
    this.latitude = latitude;
  }

  /**
   * Change the longitude of this GPS location
   *
   * @param longitude The new longitude for this GPS location
   * @throws InvalidGpsLocationException If longitude is not in range [-180.0,180.0]
   */
  public void setLongitude(Double longitude) throws InvalidGpsLocationException {
    if (longitude != null && Math.abs(longitude) > ABS_LONGITUDE_BOUND) {
      throw new InvalidGpsLocationException(
          "Longitude should be within -" + ABS_LONGITUDE_BOUND + " and " + ABS_LONGITUDE_BOUND);
    }
    this.longitude = longitude;
  }

  /**
   * Determines if the given object is logically equal to this GPS location
   *
   * @param o The object to compare
   * @return True if the object equals this GPS location
   */
  @Override
  public boolean equals(@NotNull Object o) {
    if (o == null) {
      return false;
    }
    GpsLocation other = (GpsLocation) o;
    if (!(Objects.deepEquals(this.latitude, other.getLatitude()))) {
      return false;
    }
    if (!(Objects.deepEquals(this.longitude, other.getLongitude()))) {
      return false;
    }
    return true;
  }

  /**
   * Get the weighted average of a variable number of GPS locations Manually created geotags are
   * weighted differently than automatically created geotags according to cuair.geotag.manualweight
   * in application.conf
   *
   * @param isManualGeotags a List of Booleans that corresponds to if the locations are manual or
   *     not
   * @param locations a variable number of GPS locations, passed in as varargs or an array
   * @return a new GPS location representing the average
   * @throws IllegalArgumentException if isManualGeotags and locations are not of equal length
   */
  @Nullable
  public static GpsLocation average(
      @NotNull Boolean[] isManualGeotags, @NotNull GpsLocation[] locations) {

    if (isManualGeotags.length != locations.length) {
      throw new IllegalArgumentException(
          "Manually created geotags must be of length equal to locations");
    }
    if (locations.length == 0) {
      return null;
    }

    List<Double> manualLats = new ArrayList<>();
    List<Double> manualLons = new ArrayList<>();
    List<Double> autoLats = new ArrayList<>();
    List<Double> autoLons = new ArrayList<>();

    int count = 0;
    for (int i = 0; i < locations.length; i++) {
      GpsLocation gps = locations[i];
      boolean isManual = isManualGeotags[i] != null && isManualGeotags[i];
      if (gps != null
          && gps.getLatitude() != null
          && gps.getLongitude() != null
          && !gps.getLatitude().isNaN()
          && !gps.getLongitude().isNaN()) {
        if (isManual) {
          manualLats.add(gps.getLatitude());
          manualLons.add(gps.getLongitude());
        } else {
          autoLats.add(gps.getLatitude());
          autoLons.add(gps.getLongitude());
        }
        count++;
      }
    }

    if (count == 0) {
      return null;
    }

    Double medManualLat = getMedian(manualLats);
    Double medManualLon = getMedian(manualLons);
    Double medAutoLat = getMedian(autoLats);
    Double medAutoLon = getMedian(autoLons);

    double avgLat, avgLon;

    avgLat =
        weightedAverage(
            new Double[] {medManualLat, medAutoLat},
            new Double[] {MANUAL_GEOTAG_WEIGHT, AUTO_GEOTAG_WEIGHT});
    avgLon =
        weightedAverage(
            new Double[] {medManualLon, medAutoLon},
            new Double[] {MANUAL_GEOTAG_WEIGHT, AUTO_GEOTAG_WEIGHT});

    GpsLocation average = null;

    try {
      average = new GpsLocation(avgLat, avgLon);
    } catch (InvalidGpsLocationException e) {
      // Logger.error("Invalid GPS Location. Average lat: " + avgLat + " and Average lon: " + avgLon);
    }
    return average;
  }

  @Nullable
  private static Double getMedian(@NotNull List<Double> list) {
    if (list.size() == 0) return null;
    Collections.sort(list);
    int medianIndex = list.size() / 2;
    return list.size() % 2 == 0
        ? (list.get(medianIndex) + list.get(medianIndex - 1)) / 2
        : list.get(medianIndex);
  }

  private static double weightedAverage(Double[] values, Double[] weights) {
    if (values.length != weights.length)
      throw new IllegalArgumentException(
          "Weighted average calculation must be passed equal number of values and weights");
    double valueSum = 0.0;
    double weightSum = 0.0;
    for (int i = 0; i < values.length; i++) {
      if (values[i] != null) {
        valueSum += values[i] * weights[i];
        weightSum += weights[i];
      }
    }
    return valueSum / weightSum;
  }
}
