package org.cuair.ground.models.geotag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.persistence.Embedded;
import javax.persistence.Entity;
// import org.cuair.ground.daos.AssignmentDatabaseAccessor;
// import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
// import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.CUAirModel;
import org.cuair.ground.models.Image;
// import org.cuair.ground.models.TelemetryData;
// import org.cuair.ground.models.plane.target.AlphanumTarget;
// import org.cuair.ground.models.plane.target.AlphanumTargetSighting;
// import org.cuair.ground.models.plane.target.EmergentTarget;
// import org.cuair.ground.models.plane.target.EmergentTargetSighting;
// import org.cuair.ground.models.plane.target.Target;
// import org.cuair.ground.models.plane.target.TargetSighting;
import org.cuair.ground.util.PlayConfig;
// import play.Logger;

/** Represents the position and orientation of an object on the ground */
@Entity
public class Geotag extends CUAirModel {

  /** Field of view of camera horizontally and vertically */
  private static final double FOV_HORIZONTAL_RADIANS =
      0.0;

  private static final double FOV_VERTICAL_RADIANS = 0.0;

  /** Width of height and image in pixels */
  public static final double IMAGE_WIDTH = 0.0;

  public static final double IMAGE_HEIGHT = 0.0;

  /** The database access object for the assignment table */
  // private static final AssignmentDatabaseAccessor assignmentDao =
  //     (AssignmentDatabaseAccessor)
  //         DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);

  // private static final TargetSightingsDatabaseAccessor<AlphanumTargetSighting>
  //     alphaTargetSightingDao =
  //         (TargetSightingsDatabaseAccessor<AlphanumTargetSighting>)
  //             DAOFactory.getDAO(
  //                 DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
  //                 AlphanumTargetSighting.class);

  // private static final TargetSightingsDatabaseAccessor<EmergentTargetSighting>
  //     emergentTargetSightingDao =
  //         (TargetSightingsDatabaseAccessor<EmergentTargetSighting>)
  //             DAOFactory.getDAO(
  //                 DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
  //                 EmergentTargetSighting.class);

  // private static final ClientCreatableDatabaseAccessor<AlphanumTarget> alphanumTargetDao =
  //     (ClientCreatableDatabaseAccessor<AlphanumTarget>)
  //         DAOFactory.getDAO(
  //             DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, AlphanumTarget.class);

  // private static final ClientCreatableDatabaseAccessor<EmergentTarget> emergentTargetDao =
  //     (ClientCreatableDatabaseAccessor<EmergentTarget>)
  //         DAOFactory.getDAO(
  //             DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget.class);

  /** The GPS coordinates of this geotag */
  @Embedded private GpsLocation gpsLocation;

  /** The orientation of this geotag represented as radians from north */
  private Double radiansFromNorth;

  /** True if the geotag was created from manual geotagging, false otherwise */
  private boolean isManualGeotag;

  /**
   * Creates a new geotag with the given GPS coordinates and orientation
   *
   * @param gpsLocation The GPS location of this geotag
   * @param radiansFromNorth The orientation of this geotag represented as radians from north
   * @param isManualGeotag Whether or not this Geotag was manually created or not
   */
  public Geotag(GpsLocation gpsLocation, Double radiansFromNorth, Boolean isManualGeotag) {
    this(gpsLocation, radiansFromNorth);
    this.isManualGeotag = isManualGeotag;
  }

  /**
   * Creates a new geotag with the given GPS coordinates and orientation
   *
   * @param gpsLocation The GPS location of this geotag
   * @param radiansFromNorth The orientation of this geotag represented as radians from north
   */
  public Geotag(GpsLocation gpsLocation, Double radiansFromNorth) {
    this.gpsLocation = gpsLocation;
    this.radiansFromNorth = radiansFromNorth;
  }

  public Geotag(TargetSighting sighting) {
    // Assignment assignment = sighting.getAssignment();
    // if (assignment == null) {
    //   return;
    // }
    Image image = assignment.getImage();
    if (image == null) {
      return;
    }
    // TelemetryData telemetryData = image.getTelemetryData();
    // if (telemetryData == null) {
    //   return;
    // }
    // AerialPosition aerialPosition = telemetryData.getAerialPosition();
    // if (aerialPosition == null) {
    //   return;
    // }
    double altitude = -1;
    if (telemetryData.getAerialPosition().getAltitude() != null) {
      altitude = telemetryData.getAerialPosition().getAltitude();
    }

    double pixelX = sighting.getPixelX();
    double pixelY = sighting.getPixelY();
    // double planeYaw = telemetryData.getHeadingFromNorth() * Math.PI / 180;
    // double centerLongitude = telemetryData.getAerialPosition().getLocation().getLongitude();
    // double centerLatitude = telemetryData.getAerialPosition().getLocation().getLatitude();
    // this.gpsLocation =
    //     getPixelCoordinates(centerLatitude, centerLongitude, altitude, pixelX, pixelY, planeYaw);
    // this.radiansFromNorth = getRadiansFromNorth(planeYaw, sighting.getRadiansFromTop());
  }

  public static GpsLocation getPixelCoordinates(
      double latitude,
      double longitude,
      double altitude,
      double pixelX,
      double pixelY,
      double planeYawRadians) {
    // total horizontal distance imaged in feet
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

    // distance covered per pixel in feet/pixel
    double dpphoriz = hdi / IMAGE_WIDTH;
    double dppvert = vdi / IMAGE_HEIGHT;

    // finding distance from the center
    double deltaPixelX = pixelX - (IMAGE_WIDTH / 2);
    double deltaPixelY = (IMAGE_HEIGHT / 2) - pixelY;

    double dppH = deltaPixelX * dpphoriz;
    double dppV = deltaPixelY * dppvert;

    // matrix rotation to account for the yaw - (clockwise)
    double target_reference_x_feet =
        dppH * Math.cos(planeYawRadians) + dppV * Math.sin(planeYawRadians);
    double target_reference_y_feet =
        dppH * -1 * Math.sin(planeYawRadians) + dppV * Math.cos(planeYawRadians);

    // actual:
    double latitudeFeetPerDegree = 364441.32;
    double longitudeFeetPerDegree = 269909.63;

    // adding the distance from the center to the plane's center position
    double longitude_of_target_x = longitude + target_reference_x_feet / longitudeFeetPerDegree;
    double latitude_of_target_y = latitude + target_reference_y_feet / latitudeFeetPerDegree;

    GpsLocation gps = null;
    try {
      gps = new GpsLocation(latitude_of_target_y, longitude_of_target_x);
    } catch (InvalidGpsLocationException e) {
      // Logger.error(e.getMessage());
    }
    return gps;
  }

  /**
   * Get the orientation of this geotag as radians from north
   *
   * @return The orientation of this geotag
   */
  public static Double getRadiansFromNorth(double planeYaw, double radiansFromTop) {
    return (planeYaw + radiansFromTop) % (2 * Math.PI);
  }

  /**
   * Get the GPS coordinates of this geotag
   *
   * @return The GPS coordinates of this geotag
   */
  public GpsLocation getGpsLocation() {
    return gpsLocation;
  }

  /**
   * Get the orientation of this geotag as radians from north
   *
   * @return The orientation of this geotag
   */
  public Double getRadiansFromNorth() {
    return radiansFromNorth;
  }

  /**
   * Get the boolean that represents if this geotag was created manually or not
   *
   * @return whether or not this geotag was created manually or not
   */
  public Boolean getIsManualGeotag() {
    return isManualGeotag;
  }

  /**
   * Change the GPS coordinates of this geotag
   *
   * @param gps The new GPS coordinates of this geotag
   */
  public void setGpsLocation(GpsLocation gps) {
    this.gpsLocation = gps;
  }

  /**
   * Change the orientation of this geotag
   *
   * @param radiansFromNorth The new orientation of this geotag represented as radians from north
   */
  public void setRadiansFromNorth(Double radiansFromNorth) {
    this.radiansFromNorth = radiansFromNorth;
  }

  public void setIsManualGeotag(boolean isManualGeotag) {
    this.isManualGeotag = isManualGeotag;
  }

  /**
   * Average a variable number of geotags
   *
   * @param geotags The geotag objects to average
   * @return The averaged geotag object
   */
  static Geotag average(Geotag... geotags) {
    geotags =
        Arrays.stream(geotags)
            .filter(g -> g != null)
            .filter(g -> g.getGpsLocation() != null)
            .filter(g -> g.getRadiansFromNorth() != null)
            .toArray(Geotag[]::new);
    if (geotags == null || geotags.length == 0) {
      return null;
    }
    GpsLocation[] locations =
        Arrays.stream(geotags).map(Geotag::getGpsLocation).toArray(GpsLocation[]::new);
    Double[] radians =
        Arrays.stream(geotags).map(Geotag::getRadiansFromNorth).toArray(Double[]::new);
    Boolean[] manualGeotags =
        Arrays.stream(geotags).map(Geotag::getIsManualGeotag).toArray(Boolean[]::new);
    return new Geotag(GpsLocation.average(manualGeotags, locations), Radian.average(radians));
  }

  /**
   * Checks to see if geotag can be set for target sighting
   *
   * @param sighting the target sighting
   */
  private static boolean canSetGeotag(TargetSighting sighting) {
    if (sighting instanceof AlphanumTargetSighting) {
      if (((AlphanumTargetSighting) sighting).isOffaxis()) {
        return false;
      }
    }
    // Assignment assignment = sighting.getAssignment();
    // if (assignment == null) {
    //   return false;
    // }
    Image image = assignment.getImage();
    if (image == null) {
      return false;
    }
    // TelemetryData telemetryData = image.getTelemetryData();
    // if (telemetryData == null) {
    //   return false;
    // }
    // AerialPosition aerialPosition = telemetryData.getAerialPosition();
    // if (aerialPosition == null) {
    //   return false;
    // }
    return true;
  }

  public static void updateGeotagForTargetSightings(Image img) {
    // List<Assignment> assignments = assignmentDao.getAllForImageId(img.getId());
    // List<TargetSighting> tsList = new ArrayList<>();
    // for (Assignment a : assignments) {
    //   List<AlphanumTargetSighting> tsalpha =
    //       alphaTargetSightingDao.getAllTargetSightingsForAssignment(a.getId());
    //   List<EmergentTargetSighting> tsemergent =
    //       emergentTargetSightingDao.getAllTargetSightingsForAssignment(a.getId());
    //   tsList.addAll(tsalpha);
    //   tsList.addAll(tsemergent);
    // }
    // HashSet<Target> uniqueTargets = new HashSet<>();
    // for (TargetSighting ts : tsList) {
    //   ts.setGeotag(new Geotag(ts));
    //   updateTargetSightingInDao(ts);
    //   if (ts.getTarget() != null) {
    //     uniqueTargets.add(ts.getTarget());
    //   }
    // }
    // for (Target target : uniqueTargets) {
    //   updateGeotag(target, null);
    // }
  }

  /**
   * Updates the Geotag of a target based on the Geotag of its corresponding TargetSightings
   *
   * @param targ the target
   * @param ts the target sighting
   */
  public static void updateGeotag(Target targ, TargetSighting ts) {
    // List<TargetSighting> sights = new ArrayList<TargetSighting>();

    // if (targ.fetchAssociatedTargetSightingClass() == AlphanumTargetSighting.class) {
    //   sights.addAll(alphaTargetSightingDao.getAllTargetSightingsForTarget(targ.getId()));
    // } else if (targ.fetchAssociatedTargetSightingClass() == EmergentTargetSighting.class) {
    //   sights.addAll(emergentTargetSightingDao.getAllTargetSightingsForTarget(targ.getId()));
    // } else {
    //   // Logger.warn("Target class is not Alphanum or Emergent");
    // }
    // if (ts != null && !sights.contains(ts)) sights.add(ts);
    // TargetSighting[] sightsArr = sights.toArray(new TargetSighting[sights.size()]);
    // Geotag[] geotags =
    //     Arrays.stream(sightsArr)
    //         .map(TargetSighting::getGeotag)
    //         .filter(g -> g != null)
    //         .toArray(Geotag[]::new);
    // targ.setGeotag(Geotag.average(geotags));
    // updateTargetInDao(targ);
  }

  /**
   * Checks if geotag from TargetSighting's assignment is valid. If so, sets target sighting's
   * geotag to it.
   *
   * @param ts
   * @return true if geotag was set, false otherwise
   */
  public static boolean attemptSetGeotagForTargetSighting(TargetSighting ts) {
    if (!canSetGeotag(ts)) {
      return false;
    }
    ts.setGeotag(new Geotag(ts));
    return true;
  }

  /**
   * Update target sighting's dao
   *
   * @param ts
   */
  private static void updateTargetSightingInDao(TargetSighting ts) {
    // if (ts instanceof AlphanumTargetSighting) {
    //   alphaTargetSightingDao.update((AlphanumTargetSighting) ts);
    // } else if (ts instanceof EmergentTargetSighting) {
    //   emergentTargetSightingDao.update((EmergentTargetSighting) ts);
    // } else {
      // Logger.warn("Target class is not Alphanum or Emergent");
    // }
  }

  /**
   * Update target's dao
   *
   * @param targ the target
   */
  private static void updateTargetInDao(Target targ) {
    // if (targ.fetchAssociatedTargetSightingClass() == AlphanumTargetSighting.class) {
    //   if (!alphanumTargetDao.create((AlphanumTarget) targ))
    //     alphanumTargetDao.update((AlphanumTarget) targ);
    // } else if (targ.fetchAssociatedTargetSightingClass() == EmergentTargetSighting.class) {
    //   emergentTargetDao.update((EmergentTarget) targ);
    // } else {
    //   // Logger.warn("Target class is not Alphanum or Emergent");
    // }
  }

  /**
   * Determines if the given object is logically equal to this Geotag
   *
   * @param o The object to compare
   * @return True if the object equals this Geotag
   */
  @Override
  public boolean equals(@Nonnull Object o) {
    Geotag other = (Geotag) o;

    // unsure if deepEquals will handle Radian.equals
    if (!(((this.radiansFromNorth == null) && (other.getRadiansFromNorth() == null))
        || Radian.equals(this.radiansFromNorth, other.getRadiansFromNorth()))) {
      return false;
    }

    if (!Objects.deepEquals(this.gpsLocation, other.getGpsLocation())) {
      return false;
    }

    return true;
  }
}
