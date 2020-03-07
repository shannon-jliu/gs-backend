package org.cuair.ground.models.geotag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import org.cuair.ground.util.Flags;
import org.jetbrains.annotations.Nullable;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Represents a GPS location in the world */
@Embeddable
public class GpsLocation {

  /** Maximum valid latitude */
  public static final Double ABS_LATITUDE_BOUND = 90.0;

  /** Maximum valid longitude */
  public static final Double ABS_LONGITUDE_BOUND = 180.0;

  /** The weight of the manually created geotags for weighted average */
  private static final double MANUAL_GEOTAG_WEIGHT = 0.5;//Flags.AVG_MANUAL_WEIGHT; todo

  /** The weight of the automatically created geotags for weighted average */
  private static final double AUTO_GEOTAG_WEIGHT = 0.5;//1 - Flags.AVG_MANUAL_WEIGHT; todo

  private static final Logger logger = LoggerFactory.getLogger(GpsLocation.class);

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
   * Returns the euclidean distance from this GPS location to another GPS location
   *
   * <p>Note: Euclidean Distance on GPS location doesn't make sense since the earth is round, but we
   * can assume that we won't fly that far
   *
   * @param other The GPS location to comptute against this GPS location
   * @return A non-negative distance between the two GPS locations
   */
  public double euclideanDistance(@NotNull GpsLocation other) {
    double latDiff = this.latitude - other.latitude;
    double longDiff = this.longitude - other.longitude;
    return Math.sqrt(Math.pow(latDiff, 2) + Math.pow(longDiff, 2));
  }

  public static GpsLocation average(@NotNull GpsLocation[] locations) {
    int size = locations.length;
    double latTotal = 0;
    double lonTotal = 0;
    for (GpsLocation location : locations) {
      latTotal += location.getLatitude();
      lonTotal += location.getLongitude();
    }
    try {
      return new GpsLocation(latTotal / size, lonTotal / size);
    } catch (InvalidGpsLocationException e) {
      logger.error("Calculated GPS location is invalid!\n" + e.getMessage());
      return null;
    }
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
      logger.error("Invalid GPS Location. Average lat: " + avgLat + " and Average lon: " + avgLon);
    }
    return average;
  }

  /**
   * This function changes the DMS: DegreeMinuteSecond into DD: Degrees only. DD = d + (min/60) +
   * (sec/3600) For example: 48degrees 28minutes 32seconds = 48.4756degrees
   *
   * @param values: [degrees, minutes, seconds]
   * @param reference: N: North, S: South, W: West, E:East
   */
  @Nullable
  public static Double DMStoDD(Double[] values, String reference) {
    if (values == null || reference == null) return null;
    Double result = (values[0] + values[1] / 60.0 + values[2] / 3600.0);
    int sign = (reference.equals("S") || reference.equals("W")) ? -1 : 1;
    return (result == 0) ? result : sign * result;
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



// package org.cuair.ground.models.geotag

// import java.util.Objects
// import javax.persistence.Basic
// import javax.persistence.Embeddable
// import javax.validation.constraints.NotNull
// import org.cuair.ground.models.exceptions.InvalidGpsLocationException
// import kotlin.math.abs

// /** Represents a GPS location in the world */
// @Embeddable
// class GpsLocation
//     /**
//      * Creates a new GPS location with the given latitude and longitude
//      *
//      * @param latitude The latitude of the GPS location
//      * @param longitude The longitude of the GPS location
//      * @throws InvalidGpsLocationException If latitude is not in range [-90.0,90.0]
//      * @throws InvalidGpsLocationException If longitude is not in range [-180.0,180.0]
//      */
//     @Throws(InvalidGpsLocationException::class)
//     constructor(
//         @field:Basic(optional = true)
//         private var latitude: Double,
//         @field:Basic(optional = true)
//         private var longitude: Double
//     ) {
//         init {
//             if (abs(latitude) > ABS_LATITUDE_BOUND) {
//                 throw InvalidGpsLocationException(
//                     "Latitude should be within -$ABS_LATITUDE_BOUND and $ABS_LATITUDE_BOUND"
//                 )
//             }
//             if (abs(longitude) > ABS_LONGITUDE_BOUND) {
//                 throw InvalidGpsLocationException(
//                     "Longitude should be within -$ABS_LONGITUDE_BOUND and $ABS_LONGITUDE_BOUND"
//                 )
//             }
//         }

//     /**
//      * Get the latitude of this GPS location
//      *
//      * @return the latitude of this GPS location
//      */
//     fun getLatitude(): Double {
//         return latitude
//     }

//     /**
//      * Change the latitude of this GPS location
//      *
//      * @param latitude The new latitude for this GPS location
//      * @throws InvalidGpsLocationException If latitude is not in range [-90.0,90.0]
//      */
//     @Throws(InvalidGpsLocationException::class)
//     fun setLatitude(latitude: Double) {
//         if (abs(latitude) > ABS_LATITUDE_BOUND) {
//             throw InvalidGpsLocationException(
//                 "Latitude should be within -$ABS_LATITUDE_BOUND and $ABS_LATITUDE_BOUND"
//             )
//         }
//         this.latitude = latitude
//     }

//     /**
//      * Get the longitude of this GPS location
//      *
//      * @return the longitude of this GPS location
//      */
//     fun getLongitude(): Double {
//         return longitude
//     }

//     /**
//      * Change the longitude of this GPS location
//      *
//      * @param longitude The new longitude for this GPS location
//      * @throws InvalidGpsLocationException If longitude is not in range [-180.0,180.0]
//      */
//     @Throws(InvalidGpsLocationException::class)
//     fun setLongitude(longitude: Double) {
//         if (abs(longitude) > ABS_LONGITUDE_BOUND) {
//             throw InvalidGpsLocationException(
//                 "Longitude should be within -$ABS_LONGITUDE_BOUND and $ABS_LONGITUDE_BOUND"
//             )
//         }
//         this.longitude = longitude
//     }

//     fun average(isManualGeotags: Boolean[], locations: GpsLocation[]): GpsLocation {

//         if (isManualGeotags.length != locations.length) {
//           throw new IllegalArgumentException(
//               "Manually created geotags must be of length equal to locations");
//         }
//         if (locations.length == 0) {
//           return null;
//         }

//         List<Double> manualLats = new ArrayList<>();
//         List<Double> manualLons = new ArrayList<>();
//         List<Double> autoLats = new ArrayList<>();
//         List<Double> autoLons = new ArrayList<>();

//         int count = 0;
//         for (int i = 0; i < locations.length; i++) {
//           GpsLocation gps = locations[i];
//           boolean isManual = isManualGeotags[i] != null && isManualGeotags[i];
//           if (gps != null
//               && gps.getLatitude() != null
//               && gps.getLongitude() != null
//               && !gps.getLatitude().isNaN()
//               && !gps.getLongitude().isNaN()) {
//             if (isManual) {
//               manualLats.add(gps.getLatitude());
//               manualLons.add(gps.getLongitude());
//             } else {
//               autoLats.add(gps.getLatitude());
//               autoLons.add(gps.getLongitude());
//             }
//             count++;
//           }
//         }

//         if (count == 0) {
//           return null;
//         }

//         Double medManualLat = getMedian(manualLats);
//         Double medManualLon = getMedian(manualLons);
//         Double medAutoLat = getMedian(autoLats);
//         Double medAutoLon = getMedian(autoLons);

//         double avgLat, avgLon;

//         avgLat =
//             weightedAverage(
//                 new Double[] {medManualLat, medAutoLat},
//                 new Double[] {MANUAL_GEOTAG_WEIGHT, AUTO_GEOTAG_WEIGHT});
//         avgLon =
//             weightedAverage(
//                 new Double[] {medManualLon, medAutoLon},
//                 new Double[] {MANUAL_GEOTAG_WEIGHT, AUTO_GEOTAG_WEIGHT});

//         GpsLocation average = null;

//         try {
//           average = new GpsLocation(avgLat, avgLon);
//         } catch (InvalidGpsLocationException e) {
//           Logger.error("Invalid GPS Location. Average lat: " + avgLat + " and Average lon: " + avgLon);
//         }
//         return average;
//     }

//     /**
//      * Determines if the given object is logically equal to this GPS location
//      *
//      * @param other The object to compare
//      * @return True if the object equals this GPS location
//      */
//     override fun equals(@NotNull other: Any?):Boolean {
//         if(other !is GpsLocation) return false
//         if (this.latitude != other.latitude) return false
//         if (this.longitude != other.longitude) return false
//         return true
//     }

//     // TODO add in euclidean distance

//     companion object {
//         /** Maximum valid latitude  */
//         const val ABS_LATITUDE_BOUND = 90.0
//         /** Maximum valid longitude  */
//         const val ABS_LONGITUDE_BOUND = 180.0

//         // TODO add in the rest of these methods later
//     }
// }
