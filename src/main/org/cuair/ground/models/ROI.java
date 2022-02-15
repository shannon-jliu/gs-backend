package org.cuair.ground.models;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.util.Geotagging;

@Entity
public class ROI extends ClientCreatable {

  /**
   * The assignment from which this ROI was created (contains the image that this ROI was tagged in)
   */
  @ManyToOne
  private Assignment assignment;

  /** The x pixel coordinate of the center of the ROI in the specific Image */
  private Integer pixelx;

  /** The y pixel coordinate of the center of the ROI in the specific Image */
  private Integer pixely;

  /** The GPS location of this ROI */
  @Embedded
  private GpsLocation gpsLocation;

  /** A boolean value representing if this ROI was the result of averaging a cluster */
  private Boolean averaged;

  /**
   * Creates a new non-averaged ROI
   *
   * @param creator    The ODLCUser of the ROI
   * @param pixelx     Integer x pixel coordinate of the center of the ROI in the specific Image
   * @param pixely     Integer y pixel coordinate of the center of the ROI in the specific Image
   * @param assignment The assignment that created this ROI
   */
  public ROI(
      ODLCUser creator,
      Integer pixelx,
      Integer pixely,
      Assignment assignment) {
    super(creator);
    this.assignment = assignment;
    this.pixelx = pixelx;
    this.pixely = pixely;
    this.averaged = false;
  }

  /**
   * Creates a new averaged ROI with an averaged GpsLocation
   *
   * @param creator The ODLCUser of the ROI
   * @param gps     The averaged GpsLocation for this ROI
   */
  public ROI(ODLCUser creator, GpsLocation gps) {
    super(creator);
    this.gpsLocation = gps;
    this.averaged = true;
  }

  /**
   * Returns whether this ROI is an averaged ROI or not
   *
   * @return True if averaged, false otherwise
   */
  public boolean isAveraged() {
    return this.averaged;
  }

  /**
   * Returns the associated GpsLocation of this ROI
   *
   * @return The ROI's GpsLocation
   */
  public GpsLocation getGpsLocation() {
    if (this.gpsLocation == null) {
      this.gpsLocation = calcGpsLocation();
    }
    return this.gpsLocation;
  }

  /**
   * Returns the associated pixel location on the x axis of this ROI
   *
   * @return The pixel location on the x axis
   */
  public Integer getPixelx() {
    return this.pixelx;
  }

  /**
   * Returns the associated pixel location on the y axis of this ROI
   *
   * @return The pixel location on the y axis
   */
  public Integer getPixely() {
    return this.pixely;
  }

  /**
   * Returns the assignment for this ROI
   *
   * @return the assignment set for this ROI
   */
  public Assignment getAssignment() {
    return this.assignment;
  }

  /**
   * Sets the assignment for this ROI
   *
   * @param assignment the assignment to set for this ROI
   */
  public void setAssignment(Assignment assignment) {
    this.assignment = assignment;
  }

  /**
   * Calculates the GpsLocation of this ROI
   *
   * @return A GpsLocation of the ROI or null if assignment, pixelx, or pixely is null
   */
  private GpsLocation calcGpsLocation() {
    if (this.assignment == null || this.pixelx == null || this.pixely == null) {
      return null;
    }

    Image i = this.assignment.getImage();
    if (i == null) {
      return null;
    }

    Telemetry telemetry = this.assignment.getImage().getTelemetry();
    if (telemetry == null) {
      return null;
    }

    GpsLocation assignmentGps = telemetry.getGps();
    if (assignmentGps == null) {
      return null;
    }

    double[] fov = i.getFov();

    Double latitude = assignmentGps.getLatitude();
    Double longitude = assignmentGps.getLongitude();
    Double altitude = telemetry.getAltitude();
    Double planeYaw = telemetry.getPlaneYaw();

    if (latitude == null || longitude == null || altitude == null || planeYaw == null) {
      return null;
    }

    return Geotagging
        .getPixelCoordinates(latitude, longitude, altitude, fov, this.pixelx, this.pixely, planeYaw);
  }
}
