package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.ok;

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
@CrossOrigin
@RestController
@RequestMapping(value = "/progress")
//TODO: fix naming conventions...
public class PlaneSystemController {
  private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
  private SettingsClient sc = new SettingsClient();
  /**
   * Starts pan search
   *
   * @return 200 on success, 400 on error
   * // TODO: potentially change post methods to get for the ps mode questions
   * // TODO: figure out what is returned
   */
  @RequestMapping(value = "/pan-search", method = RequestMethod.POST)
  public ResponseEntity startPanSearch() {
    try {
      sc.setPanSearch();
    } catch (Exception e) {
      logger.info("error with start pan search " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: pan search");
    }
    return ok("started panning");
  }

  /**
   * @return 200 on success, 400 on error
   * changes the ps mode to manual-search
   */
  @RequestMapping(value = "/manual-search", method = RequestMethod.POST)
  public ResponseEntity toggleManualSearch() {
    try {
      sc.setManualSearch();
    } catch (Exception e) {
      logger.info("error with toggling manual search " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: manual search");
    }
    return ok("toggled manual search");
  }

  /**
   * @return 200 on success, 400 on error
   * changes the ps mode to distance-search
   */
  @RequestMapping(value = "/distance-search", method = RequestMethod.POST)
  public ResponseEntity startDistanceSearch() {
    try {
      sc.setDistanceSearch();
    } catch (Exception e) {
      logger.info("error with starting distance search " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: distance search");
    }
    return ok("running distance search");
  }

  /**
   * @return 200 on success, 400 on error
   * changes the ps mode to time-search
   */
  @RequestMapping(value = "/time-search", method = RequestMethod.POST)
  public ResponseEntity startTimeSearch(@RequestHeader("inactive") Integer inactiveTime, @RequestHeader("active") Integer activeTime) {
    if (activeTime >= 0 && inactiveTime >= 0) {
      try {
        sc.setTimeSearch(inactiveTime, activeTime);
      } catch (Exception e) {
        logger.info("error with starting time search " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: time search");
      }
      return ok("running time search");
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid inactive & active times, inactive time: " + inactiveTime + ", active time: " + activeTime);
  }

  /**
   * @return 200 on success, 400 on error
   * calls plane system with new pitch & roll values, sends request to gimbal to move to new position
   */
  @RequestMapping(value = "/set-gimbal", method = RequestMethod.POST)
  public ResponseEntity setGimbal(@RequestHeader("roll") Float roll, @RequestHeader("pitch") Float pitch) {
    if (roll >= 0 && pitch >= 0) {
      try {
        sc.setGimbalPosition(roll, pitch);
      } catch (Exception e) {
        logger.info("error with setting gimbal settings on ps side " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: gimbal");
      }
      return ok("Gimbal position changed successfully, roll: " + roll + " pitch: " + pitch);
    }
    logger.info("Error: gimbal " + roll + " " + pitch);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid roll and pitch values, roll: " + roll + ", pitch: " + pitch);
  }

  /**
   * @return 200 on success, 400 on error
   * takes json with f32 field “focal_length”
   */
  @RequestMapping(value = "/focal-len", method = RequestMethod.POST)
  public ResponseEntity setFocalLength(@RequestHeader("focalLength") Float focalLength) {
    if (focalLength != null && focalLength >= 0) {
      logger.info("Focal length post request: " + focalLength);
      try {
        sc.setFocalLength(focalLength);
      } catch (Exception e) {
        logger.info("error with focal length post " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: focal length");
      }
      return ok("Focal length updated successfully " + focalLength);
    }
    logger.info("error with focal length seting");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid focal length values " + focalLength);
  }

  /**
   * @return 200 on success, 400 on error
   * /set-zoom-level, takes json with u8 field “level” (0-60, any past 30 is digital zoom)
   */
  @RequestMapping(value = "/set-zoom-level", method = RequestMethod.POST)
  public ResponseEntity setZoomLevel (@RequestHeader("level") Float level) {
    if (level >= 0 && level <= 60) {
      logger.info("set zoom level post request: " + level);
      try {
        sc.setZoomLevel(level);
      } catch (Exception e) {
        logger.info("error with setting zoom level " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: zoom level setting");
      }
      return ok("Zoom level updated successfully " + level);
    }
    logger.info("error with zoom level");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid zoom level input " + level);
  }

  /**
   * @return 200 on success, 400 on error
   * /set-aperture, set the aperture, json with value “aperture”: u16 → pass an int!
   */
  @RequestMapping(value = "/set-aperture", method = RequestMethod.POST)
  public ResponseEntity setAperture (@RequestHeader("aperture") Integer aperture) {
    if (aperture >= 0) {
      logger.info("set aperture post request: " + aperture);
      try {
        sc.setAperture(aperture);
      } catch (Exception e) {
        logger.info("error with setting aperture " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: aperture");
      }
      return ok("aperture updated successfully " + aperture);
    }
    logger.info("error with aperture");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid aperture input " + aperture);
  }

  /**
   * @return 200 on success, 400 on error
   * /set-shutter-speed, sets the shutter speed, json with values numerator, denominator, both u16
   */
  @RequestMapping(value = "/set-shutter-speed", method = RequestMethod.POST)
  public ResponseEntity setZoomLevel (@RequestHeader("numerator") Integer numerator, @RequestHeader("denominator") Integer denominator) {
    if (numerator >= 0 && denominator > 60) {
      logger.info("set shutter speed post request: " + numerator + " " + denominator);
      try {
        sc.setShutterSpeed(numerator, denominator);
      } catch (Exception e) {
        logger.info("error with setting shutter speed " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: shutter speed");
      }
      return ok("shutter speed updated successfully " + numerator + " " + denominator);
    }
    logger.info("error with shutter speed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid shutter speed input " + numerator + " " + denominator);
  }

  /**
   * @return 200 on success, 400 on error
   * /capture, takes a single image. Don’t call when ps modes are running for now.
   */
  @RequestMapping(value = "/capture", method = RequestMethod.POST)
  public ResponseEntity capture () {
    // TODO: check if psmodes not running
    logger.info("capture image request");
    try {
      sc.capture();
    } catch (Exception e) {
        logger.info("error with capture " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: with capture");
    }
    logger.info("error with capture");
    return ok("image finished capturing");
  }

  /**
   * @return 200 on success, 400 on error
   * /get-zoom-level, return the zoom level as an integer (0-60)
   */
  @RequestMapping(value = "/get-zoom-level", method = RequestMethod.GET)
  public ResponseEntity getZoomLevel () {
    logger.info("getting zoom level");
    try {
      sc.getZoomLevel();
    } catch (Exception e) {
      logger.info("error with getting the zoom level " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: zoom level");
    }
    logger.info("error with zoom level get");
    return ok("image finished capturing");
  }
}
