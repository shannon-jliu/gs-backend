package org.cuair.ground.models.geotag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.CUAirModel;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;
import org.cuair.ground.models.plane.target.EmergentTarget;
import org.cuair.ground.models.plane.target.EmergentTargetSighting;
import org.cuair.ground.models.plane.target.Target;
import org.cuair.ground.models.plane.target.TargetSighting;
import org.cuair.ground.util.Geotagging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Represents the position and orientation of an object on the ground */
@Entity
public class Geotag extends CUAirModel {
  private static final Logger logger = LoggerFactory.getLogger(GpsLocation.class);
  /** The database access object for the assignment table */
  private static final AssignmentDatabaseAccessor assignmentDao =
      (AssignmentDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
  private static final TargetSightingsDatabaseAccessor<AlphanumTargetSighting>
      alphaTargetSightingDao =
      (TargetSightingsDatabaseAccessor<AlphanumTargetSighting>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
              AlphanumTargetSighting.class);
  private static final TargetSightingsDatabaseAccessor<EmergentTargetSighting>
      emergentTargetSightingDao =
      (TargetSightingsDatabaseAccessor<EmergentTargetSighting>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR,
              EmergentTargetSighting.class);
  private static final ClientCreatableDatabaseAccessor<AlphanumTarget> alphanumTargetDao =
      (ClientCreatableDatabaseAccessor<AlphanumTarget>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, AlphanumTarget.class);
  private static final ClientCreatableDatabaseAccessor<EmergentTarget> emergentTargetDao =
      (ClientCreatableDatabaseAccessor<EmergentTarget>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget.class);
  /** The GPS coordinates of this geotag */
  @Embedded
  private GpsLocation gpsLocation;
  /** The orientation of this geotag represented as radians from north clockwise (NE is clockwise from N, etc.) */
  private Double clockwiseRadiansFromNorth;

  /**
   * Creates a new geotag with the given GPS coordinates and orientation
   *
   * @param gpsLocation               The GPS location of this geotag
   * @param clockwiseRadiansFromNorth The orientation of this geotag represented as radians from north
   */
  public Geotag(GpsLocation gpsLocation, Double clockwiseRadiansFromNorth) {
    this.gpsLocation = gpsLocation;
    this.clockwiseRadiansFromNorth = clockwiseRadiansFromNorth;
  }

  /**
   * Creates a new geotag with the target sighting
   *
   * @param sighting The TargetSighting of this geotag
   */
  public Geotag(TargetSighting sighting) {
    if (sighting == null) {
      return;
    }
    Assignment assignment = sighting.getAssignment();
    if (assignment == null) {
      return;
    }
    Image image = assignment.getImage();
    if (image == null) {
      return;
    }
    Telemetry telemetry = image.getTelemetry();
    if (telemetry == null) {
      return;
    }
    GpsLocation gps = telemetry.getGps();
    if (gps == null || (Double) telemetry.getAltitude() == null) {
      return;
    }
    double altitude = -1;
    if ((Double) telemetry.getAltitude() != null) {
      altitude = telemetry.getAltitude();
    }

    double pixelX = sighting.getpixelX();
    double pixelY = sighting.getpixelY();
    double planeYaw = telemetry.getPlaneYaw() * Math.PI / 180;
    double centerLongitude = telemetry.getGps().getLongitude();
    double centerLatitude = telemetry.getGps().getLatitude();
    this.gpsLocation = Geotagging
        .getPixelCoordinates(centerLatitude, centerLongitude, altitude, pixelX, pixelY, planeYaw);
    this.clockwiseRadiansFromNorth =
        Geotagging.calculateClockwiseRadiansFromNorth(planeYaw, sighting.getRadiansFromTop());
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
    Assignment assignment = sighting.getAssignment();
    if (assignment == null) {
      return false;
    }
    Image image = assignment.getImage();
    if (image == null) {
      return false;
    }
    Telemetry telemetry = image.getTelemetry();
    if (telemetry == null) {
      return false;
    }
    GpsLocation gps = telemetry.getGps();
    double altitude = telemetry.getAltitude();
    return gps != null && (Double) telemetry.getAltitude() != null;
  }

  /**
   * Updates the Geotag of a target based on the Geotag of its corresponding TargetSightings
   *
   * @param targ the target
   * @param ts   the target sighting
   */
  public static void updateGeotag(Target targ, TargetSighting ts) {
    List<TargetSighting> sights = new ArrayList<TargetSighting>();

    if (targ.fetchAssociatedTargetSightingClass() == AlphanumTargetSighting.class) {
      sights.addAll(alphaTargetSightingDao.getAllTargetSightingsForTarget(targ.getId()));
    } else if (targ.fetchAssociatedTargetSightingClass() == EmergentTargetSighting.class) {
      sights.addAll(emergentTargetSightingDao.getAllTargetSightingsForTarget(targ.getId()));
    } else {
      logger.warn("Target class is not Alphanum or Emergent");
    }
    if (ts != null && !sights.contains(ts)) {
      sights.add(ts);
    }
    TargetSighting[] sightsArr = sights.toArray(new TargetSighting[sights.size()]);
    Geotag[] geotags =
        Arrays.stream(sightsArr)
            .map(TargetSighting::getGeotag)
            .filter(g -> g != null)
            .toArray(Geotag[]::new);
    targ.setGeotag(Geotagging.average(geotags));
    updateTargetInDao(targ);
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
   * Update target's dao
   *
   * @param targ the target
   */
  private static void updateTargetInDao(Target targ) {
    if (targ.fetchAssociatedTargetSightingClass() == AlphanumTargetSighting.class) {
      if (!alphanumTargetDao.create((AlphanumTarget) targ)) {
        alphanumTargetDao.update((AlphanumTarget) targ);
      }
    } else if (targ.fetchAssociatedTargetSightingClass() == EmergentTargetSighting.class) {
      emergentTargetDao.update((EmergentTarget) targ);
    } else {
      logger.warn("Target class is not Alphanum or Emergent");
    }
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
  public Double getClockwiseRadiansFromNorth() {
    return clockwiseRadiansFromNorth;
  }

  /**
   * Determines if the given object is logically equal to this Geotag
   *
   * @param o The object to compare
   * @return True if the object equals this Geotag
   */
  @Override
  public boolean equals(Object o) {
    Geotag other = (Geotag) o;
    // unsure if deepEquals will handle Radian.equals
    if (!(
        ((this.clockwiseRadiansFromNorth == null) && (other.getClockwiseRadiansFromNorth() == null))
            ||
            Radian.equals(this.clockwiseRadiansFromNorth, other.getClockwiseRadiansFromNorth()))) {
      return false;
    }
    return Objects.deepEquals(this.gpsLocation, other.getGpsLocation());
  }
}
