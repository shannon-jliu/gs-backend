package org.cuair.ground.models.geotag;

/** Exception thrown when a GpsLocation with invalid latitude or longitude is created */
public class InvalidGpsLocationException extends Exception {
  public InvalidGpsLocationException(String message) {
    super(message);
  }
}
