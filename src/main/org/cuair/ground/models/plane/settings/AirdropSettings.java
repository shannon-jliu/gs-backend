package org.cuair.ground.models.plane.settings;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import org.cuair.ground.models.geotag.GpsLocation;

/** Model to represent the settings for Airdrop */
@Entity
public class AirdropSettings extends PlaneSettingsModel {

  /** Gps Location of airdrop target in (latitude, longitude) */
  @Embedded private GpsLocation gpsTargetLocation;

  /** The allowed drop accuracy (suggested < 150ft) from center of target to meet threshold */
  private Double acceptableThresholdFt;

  /** Arm status, 1 when "ARM", 0 when "DISARM" */
  private Boolean isArmed;

  /** Status of manual override to drop package */
  private Boolean commandDropNow;

  /** 
   * AirdropSettings representation
   * @param gpsTargetLocation gps location of where to have the package land
   * @param acceptableThresholdFt the acceptable threshold of feet from the gpsTargetLocation we can have the package land on
   * @param isArmed if armed, airdrop could send drop command any second
   * @param commandDropNow if true, airdrop forgoes any math calculations and drops immediately
   */
  public AirdropSettings(
      GpsLocation gpsTargetLocation,
      Double acceptableThresholdFt,
      Boolean isArmed,
      Boolean commandDropNow) {
    this.gpsTargetLocation = gpsTargetLocation;
    this.acceptableThresholdFt = acceptableThresholdFt;
    this.isArmed = isArmed;
    this.commandDropNow = commandDropNow;
  }

  /**
   * Returns the Airdrop target gps location
   *
   * @return gps_location that is a latitude and longitude
   */
  public GpsLocation getGpsTargetLocation() {
    return gpsTargetLocation;
  }

  /**
   * Modifies the Airdrop target gps location
   *
   * @param gpsTargetLocation that is a latitude and longitude
   */
  public void setGpsTargetLocation(GpsLocation gpsTargetLocation) {
    this.gpsTargetLocation = gpsTargetLocation;
  }

  /**
   * Returns the threshold of AirdropSettings
   *
   * @return acceptableThresholdFt that measures minimum distance in ft from target
   */
  public Double getAcceptableThresholdFt() {
    return acceptableThresholdFt;
  }

  /**
   * Modifies the threshold of AirdropSettings
   *
   * @param acceptableThresholdFt that measures minimum distance in ft from target
   */
  public void setAcceptableThresholdFt(Double acceptableThresholdFt) {
    this.acceptableThresholdFt = acceptableThresholdFt;
  }

  /**
   * Returns current arm status, true for ARM, false for DISARM
   *
   * @return arm_status that indicates whether Airdrop is Armed or Disarmed
   */
  public Boolean getIsArmed() {
    return isArmed;
  }

  /**
   * Modifies arm status to ARM or DISARM, true for ARM, false for DISARM
   *
   * @param status that indicates whether Airdrop should be Armed or Disarmed
   */
  public void setIsArmed(Boolean status) {
    this.isArmed = status;
  }

  /**
   * Returns status of command_drop_now, true if "drop immediately", false if "do not drop"
   *
   * @return command_drop_now that indicates override to drop
   */
  public Boolean getCommandDropNow() {
    return this.commandDropNow;
  }

  /**
   * Overrides the status of command_drop_now, set true if "drop immediately", false if "do not
   * drop"
   *
   * @param status that indicates override to drop
   */
  public void setCommandDropNow(Boolean status) {
    this.commandDropNow = status;
  }

  /**
   * Equals override
   *
   * @param obj the object to compare
   * @return boolean true if object is equal to this airdropsetting
   */
  @Override
  public boolean equals(Object obj) {
    AirdropSettings as = (AirdropSettings) obj;

    if ((as == null)
        || this.getCommandDropNow() != as.getCommandDropNow()
        || this.getIsArmed() != as.getIsArmed()
        || !this.getAcceptableThresholdFt().equals(as.getAcceptableThresholdFt())
        || !this.getGpsTargetLocation().equals(as.getGpsTargetLocation())) {
      return false;
    }

    return true;
  }
}
