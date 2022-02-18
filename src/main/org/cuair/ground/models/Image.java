package org.cuair.ground.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import org.cuair.ground.models.geotag.FOV;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.util.Geotagging;

@Entity
public class Image extends TimestampModel {

  /** The URL where clients can retrieve the image file  */
  private String imageUrl;

  /** The local URL where image file lives on the ground server */
  private String localImageUrl;

  /** Closest Telemetry for when this Image was taken  */
  @OneToOne(cascade = CascadeType.ALL)
  private Telemetry telemetry;

  /** The type of this image. Either FIXED, TRACKING, or OFFAXIS */
  private ImgMode imgMode;

  /** True if has at least one associated MDLC assignment, otherwise false. */
  @JsonIgnore
  private boolean hasMdlcAssignment = false;

  /** True if has at least one associated ADLC assignment, otherwise false. */
  @JsonIgnore
  private boolean hasAdlcAssignment = false;

  /** The field of view of this image. */
  @OneToOne(cascade = CascadeType.ALL)
  private FOV fov;

  /** The possible image modes: fixed, tracking, and off-axis */
  public enum ImgMode {
    FIXED("fixed"),
    TRACKING("tracking"),
    OFFAXIS("off-axis");

    @JsonValue String value;
    ImgMode(String value) { this.value = value; }
  }

  // Constructor
  public Image (String imageUrl, Telemetry telemetry, FOV fov, ImgMode imgMode) {
    this.imageUrl = imageUrl;
    this.telemetry = telemetry;
    this.fov = fov;
    this.imgMode = imgMode;
  }

  // Constructor corresponding to original Image class (used in tests)
  public Image (String imageUrl, Telemetry telemetry, ImgMode imgMode, boolean hasMdlcAssignment, boolean hasAdlcAssignment, double fov) {
    this.imageUrl = imageUrl;
    this.telemetry = telemetry;
    this.fov = new FOV(fov, fov);
    this.imgMode = imgMode;
    this.hasMdlcAssignment = hasMdlcAssignment;
    this.hasAdlcAssignment = hasAdlcAssignment;
  }

  /**
   * Internal method for finding geotags corresponding to four corners of image
   */
  @JsonIgnore
  public Map<String, Object> getLocations() {
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

    Map<String, Object> locs = new HashMap<>();
    locs.put("topLeft", topLeft);
    locs.put("topRight", topRight);
    locs.put("bottomLeft", bottomLeft);
    locs.put("bottomRight", bottomRight);

    return locs;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getLocalImageUrl() {
    return localImageUrl;
  }

  public Telemetry getTelemetry() {
    return telemetry;
  }

  public ImgMode getImgMode() {
    return imgMode;
  }

  public FOV getFov() {
    return fov;
  }

  public boolean getHasMdlcAssignment() {
    return hasMdlcAssignment;
  }

  public boolean getHasAdlcAssignment() {
    return hasAdlcAssignment;
  }

  public void setLocalImageUrl(String url) {
    localImageUrl = url;
  }

  public void setHasMdlcAssignment(boolean has) {
    hasMdlcAssignment = has;
  }

  public void setHasAdlcAssignment(boolean has) {
    hasAdlcAssignment = has;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setTelemetry(Telemetry telemetry) {
    this.telemetry = telemetry;
  }

  public void setImgMode(ImgMode imgMode) {
    this.imgMode = imgMode;
  }

  public void setFov(FOV fov) {
    this.fov = fov;
  }


}
