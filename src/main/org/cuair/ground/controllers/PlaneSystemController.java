package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.ok;

import java.util.HashMap;
import java.util.Map;
import org.cuair.ground.clients.AutopilotClient;
import org.cuair.ground.clients.CameraGimbalClient;
import org.cuair.ground.clients.SettingsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to call endpoints on the plane system to update or get settings.
 */
// TODO: refactor the endpoint names so that they are consistent from frontend
// -> backend -> planesystem
// or don't and just document how the urls work (like where they are stored in
// Flags and stuff)
@CrossOrigin
@RestController
@RequestMapping(value = "/progress")
public class PlaneSystemController {
  private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
  private final SettingsClient sc = new SettingsClient();

  // GET REQUESTS:
  /**
   * @return 200 on success, 400 on error
   *         /capture, takes a single image. Don’t call when ps modes are running
   *         for now.
   */
  @RequestMapping(value = "/capture", method = RequestMethod.GET)
  public ResponseEntity capture() {
    // TODO: check that ps modes are not running
    try {
      logger.info("calling sc.capture()");
      sc.capture();
    } catch (Exception e) {
      logger.info("Error with capture in PlaneSystemController.java " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: with capture");
    }
    return ok("Image finished capturing");
  }

  /**
   * @return 200 on success, 400 on error
   *         Returns json with ints: “shutter_speed_num” and “shutter_speed_den”,
   *         “aperture”, and “iso,”
   *         and strings: “exposure_mode” and “focus_mode”
   */
  @RequestMapping(value = "/get-status", method = RequestMethod.GET)
  public ResponseEntity getStatus() {
    // temp hardcoded json for testing
    try {
      return sc.getStatus();
    } catch (Exception e) {
      logger.info("Error with getting the status " + e.getMessage());
      // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: get
      // status");
      Map<String, Object> map = new HashMap<>();
      map.put("shutter_speed_num", 1); // int
      map.put("shutter_speed_den", 2); // int
      map.put("aperture", 3); // int
      map.put("iso", 4); // int
      map.put("exposure_mode", "some exposure mode"); // string
      map.put("focus_mode", "some focus mode"); // string

      return new ResponseEntity<Object>(map, HttpStatus.OK);
    }
  }

  /**
   * Starts pan search
   * 
   * @return 200 on success, 400 on error
   *         TODO: figure out what is returned
   */
  @RequestMapping(value = "/pan-search", method = RequestMethod.GET)
  public ResponseEntity startPanSearch() {
    try {
      sc.setPanSearch();
    } catch (Exception e) {
      logger.info("Error with start pan search " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: pan search");
    }
    return ok("started panning");
  }

  /**
   * @return 200 on success, 400 on error
   *         changes the ps mode to manual-search
   */
  @RequestMapping(value = "/manual-search", method = RequestMethod.GET)
  public ResponseEntity toggleManualSearch() {
    try {
      sc.setManualSearch();
    } catch (Exception e) {
      logger.info("Error with toggling manual search " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: manual search");
    }
    return ok("Toggled manual search");
  }

  /**
   * @return 200 on success, 400 on error
   *         changes the ps mode to distance-search
   */
  @RequestMapping(value = "/distance-search", method = RequestMethod.GET)
  public ResponseEntity startDistanceSearch() {
    try {
      sc.setDistanceSearch();
    } catch (Exception e) {
      logger.info("Error with starting distance search " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: distance search");
    }
    return ok("Running distance search");
  }

  /**
   * @return 200 on success, 400 on error
   *         changes the ps mode to time-search
   */
  @RequestMapping(value = "/time-search", method = RequestMethod.GET)
  public ResponseEntity startTimeSearch(@RequestHeader("inactive") Integer inactiveTime,
      @RequestHeader("active") Integer activeTime) {
    if (activeTime >= 0 && inactiveTime >= 0) {
      try {
        sc.setTimeSearch(inactiveTime, activeTime);
      } catch (Exception e) {
        logger.info("Error with starting time search " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: time search");
      }
      return ok("Running time search");
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        "Error, invalid inactive & active times, inactive time: " + inactiveTime + ", active time: " + activeTime);
  }

  // POST REQUESTS
  /**
   * @return 200 on success, 400 on error
   *         calls plane system with new pitch & roll values, sends request to
   *         gimbal to move to new position
   */
  @RequestMapping(value = "/set-gimbal", method = RequestMethod.POST)
  public ResponseEntity controlGimbal(@RequestHeader("roll") Float roll, @RequestHeader("pitch") Float pitch) {
    if (roll >= 0 && pitch >= 0) {
      try {
        sc.controlGimbal(roll, pitch);
      } catch (Exception e) {
        logger.info("Error with setting gimbal settings on ps side " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: gimbal");
      }
      return ok("Gimbal position changed successfully, roll: " + roll + " pitch: " + pitch);
    }
    // TODO: see if these additional logs are needed when testing
    logger.info("Error: gimbal " + roll + " " + pitch);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("Error, invalid roll and pitch values, roll: " + roll + ", pitch: " + pitch);
  }

  /**
   * @return 200 on success, 400 on error
   *         takes json with f32 field “focal_length”
   */
  @RequestMapping(value = "/focal-len", method = RequestMethod.POST)
  public ResponseEntity setFocalLength(@RequestHeader("focalLength") Float focalLength) {
    if (focalLength != null && focalLength >= 0) {
      try {
        sc.setFocalLength(focalLength);
      } catch (Exception e) {
        logger.info("Error with focal length post " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: focal length");
      }
      return ok("Focal length updated successfully " + focalLength);
    }
    logger.info("Error with focal length setting");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid focal length values " + focalLength);
  }

  /**
   * @return 200 on success, 400 on error
   *         /set-zoom-level, takes json with u8 field “level” (0-60, any past 30
   *         is digital zoom)
   */
  @RequestMapping(value = "/set-zoom-level", method = RequestMethod.POST)
  public ResponseEntity setZoomLevel(@RequestHeader("level") Integer level) {
    if (level >= 0 && level <= 60) {
      try {
        sc.setZoomLevel(level);
      } catch (Exception e) {
        logger.info("Error with setting zoom level " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: zoom level setting");
      }
      return ok("Zoom level updated successfully " + level);
    }
    logger.info("Error with zoom level");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid zoom level input " + level);
  }

  /**
   * @return 200 on success, 400 on error
   *         /set-aperture, set the aperture, json with value “aperture”: u16 →
   *         pass an int!
   */
  @RequestMapping(value = "/set-aperture", method = RequestMethod.POST)
  public ResponseEntity setAperture(@RequestHeader("aperture") Integer aperture) {
    if (aperture >= 0) {
      try {
        sc.setAperture(aperture);
      } catch (Exception e) {
        logger.info("Error with setting aperture " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: aperture");
      }
      return ok("Aperture updated successfully " + aperture);
    }
    logger.info("Error with aperture");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid aperture input " + aperture);
  }

  /**
   * @return 200 on success, 400 on error
   *         /set-shutter-speed, sets the shutter speed, json with values
   *         numerator, denominator, both u16
   */
  @RequestMapping(value = "/set-shutter-speed", method = RequestMethod.POST)
  public ResponseEntity setShutterSpeed(@RequestHeader("numerator") Integer numerator,
      @RequestHeader("denominator") Integer denominator) {
    if (numerator >= 0 && denominator > 0) {
      try {
        sc.setShutterSpeed(numerator, denominator);
      } catch (Exception e) {
        logger.info("Error with setting shutter speed " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: shutter speed");
      }
      return ok("Shutter speed updated successfully " + numerator + " " + denominator);
    }
    logger.info("Error with shutter speed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("Error, invalid shutter speed input " + numerator + " " + denominator);
  }

  /**
   * 
   * @param mode
   * @return 200 on success, 400 on error
   */
  @RequestMapping(value = "/set-exposure-mode", method = RequestMethod.POST)
  public ResponseEntity setExposureMode(@RequestHeader("mode") String mode) {
    // TODO: add string validation
    // make [] of valid inputs from the notion
    try {
      sc.setExposureMode(mode);
    } catch (Exception e) {
      logger.info("Errors with setting exposure mode " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: exposure mode");
    }
    logger.info("successfully updated exposure mode");
    return ok("Exposure mode updated successfully " + mode);
  }
}
