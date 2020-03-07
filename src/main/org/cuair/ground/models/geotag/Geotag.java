package org.cuair.ground.models.geotag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;
import org.cuair.ground.util.Flags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Represents the position and orientation of an object on the ground */
@Entity
public class Geotag extends CUAirModel {

    /** Field of view of camera horizontally and vertically */
    private static double FOV_HORIZONTAL_RADIANS = Flags.FOV_HORIZONTAL_RADIANS;

    private static double FOV_VERTICAL_RADIANS = Flags.FOV_VERTICAL_RADIANS;

    /** Width of height and image in pixels */
    public static double IMAGE_WIDTH = Flags.IMAGE_WIDTH;

    public static double IMAGE_HEIGHT = Flags.IMAGE_HEIGHT;

    /** A logger */
    private static final Logger logger = LoggerFactory.getLogger(Geotag.class);

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
        // TODO: Should this be && or ||?
        if (gps == null && (Double) telemetry.getAltitude() == null) {
            return;
        }
        double altitude = -1;
        if ((Double) telemetry.getAltitude() != null) {
            altitude = telemetry.getAltitude();
        }

        double pixel_x = sighting.getpixel_x();
        double pixel_y = sighting.getpixel_y();
        double planeYaw = telemetry.getPlaneYaw() * Math.PI / 180;
        double centerLongitude = telemetry.getGps().getLongitude();
        double centerLatitude = telemetry.getGps().getLatitude();
        this.gpsLocation = getPixelCoordinates(centerLatitude, centerLongitude, altitude, pixel_x, pixel_y, planeYaw);
        this.radiansFromNorth = getRadiansFromNorth(planeYaw, sighting.getRadiansFromTop());
    }

    public static GpsLocation getPixelCoordinates(
        double latitude,
        double longitude,
        double altitude,
        double pixel_x,
        double pixel_y,
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
        double deltapixel_x = pixel_x - (IMAGE_WIDTH / 2);
        double deltapixel_y = (IMAGE_HEIGHT / 2) - pixel_y;

        double dppH = deltapixel_x * dpphoriz;
        double dppV = deltapixel_y * dppvert;

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
        // TODO: Fix this. For some reason when uncommented, it thinks there is an uncaught exception
        try {
            gps = new GpsLocation(latitude_of_target_y, longitude_of_target_x);
        } catch (InvalidGpsLocationException e) {
            logger.error(e.getMessage());
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
     * Change the manually-tagged flag of t of this geotag
     *
     * @param isManualGeotag Whether or not this geotag was done manually
     */
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
        //return null;
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
        System.out.println("after oxx axis");
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
        // TODO: Should this be && or ||?
        if (gps == null && (Double) telemetry.getAltitude() == null) {
            return false;
        }
        System.out.println("can lmfao");
        return true;
    }

    /**
     * Updates the Geotag of a target based on the Geotag of its corresponding TargetSightings
     *
     * @param targ the target
     * @param ts the target sighting
     */
    public static void updateGeotag(Target targ, TargetSighting ts) {
        System.out.println("updateGeotag");
        List<TargetSighting> sights = new ArrayList<TargetSighting>();

        if (targ.fetchAssociatedTargetSightingClass() == AlphanumTargetSighting.class) {
            sights.addAll(alphaTargetSightingDao.getAllTargetSightingsForTarget(targ.getId()));
        } else if (targ.fetchAssociatedTargetSightingClass() == EmergentTargetSighting.class) {
            sights.addAll(emergentTargetSightingDao.getAllTargetSightingsForTarget(targ.getId()));
        } else {
            logger.warn("Target class is not Alphanum or Emergent");
        }
        System.out.println("sights size " + sights.size());
        if (ts != null && !sights.contains(ts)) sights.add(ts);
        System.out.println("sights size after " + sights.size());
        TargetSighting[] sightsArr = sights.toArray(new TargetSighting[sights.size()]);
        Geotag[] geotags =
            Arrays.stream(sightsArr)
                .map(TargetSighting::getGeotag)
                .filter(g -> g != null)
                .toArray(Geotag[]::new);
        targ.setGeotag(Geotag.average(geotags));
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
            System.out.println("CANT SET GEOTAG");
            return false;
        }
        System.out.println("CAN INDEED SET GEOTAG");
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
              System.out.println("updating alphanum target " + targ.getGeotag());
              alphanumTargetDao.update((AlphanumTarget) targ);
            }
        } else if (targ.fetchAssociatedTargetSightingClass() == EmergentTargetSighting.class) {
            emergentTargetDao.update((EmergentTarget) targ);
        } else {
            logger.warn("Target class is not Alphanum or Emergent");
        }
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
