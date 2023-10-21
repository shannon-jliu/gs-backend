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
   * @return
   * changes the ps mode to pan-search
   * // TODO: potentially change post methods to get for the ps mode questions
   */
  @RequestMapping(value = "/pan-search", method = RequestMethod.POST)
  public ResponseEntity startPanSearch() {
    try {
      sc.getPanSearch();
    } catch (Exception e) {
      logger.info("error with calling plane system... " + e.getMessage());
    }
    // TODO: add actual toggle call to start search - does it just start or does it toggle?
    return ok().body("started panning");
  }

  /**
   * @return
   * changes the ps mode to manual-search
   */
  @RequestMapping(value = "/manual-search", method = RequestMethod.POST)
  public ResponseEntity toggleManualSearch() {
    // TODO: add actual toggle call - if call fails, return err
    try {
      sc.getManualSearch();
    } catch (Exception e) {
      logger.info("error... " + e.getMessage());
    }
    return ok().body("toggled manual search");
  }

  /**
   * @return
   * changes the ps mode to distance-search
   */
  @RequestMapping(value = "/distance-search", method = RequestMethod.POST)
  public ResponseEntity startDistanceSearch() {
    // TODO: add actual toggle call
    try {
      sc.getDistanceSearch();
    } catch (Exception e) {
      logger.info("error... " + e.getMessage());
    }
    return ok().body("running distance search");
  }

  /**
   * @return
   * changes the ps mode to time-search
   */
  @RequestMapping(value = "/time-search", method = RequestMethod.POST)
  public ResponseEntity startTimeSearch(@RequestHeader("inactive") Integer inactiveTime, @RequestHeader("active") Integer activeTime) {
    // TODO: add actual toggle call
    if (activeTime >= 0 && inactiveTime >= 0) {
      try {
        sc.getTimeSearch(inactiveTime, activeTime);
      } catch (Exception e) {
        logger.info("error... " + e.getMessage());
      }
      return ok().body("running time search");
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid inactive & active times, inactive time: " + inactiveTime + ", active time: " + activeTime);
  }

  /**
   * @return something
   * calls plane system with new pitch & roll values, sends request to gimbal to move to new position
   */
  @RequestMapping(value = "/set-gimbal", method = RequestMethod.POST)
  public ResponseEntity setGimbal(@RequestHeader("roll") Float roll, @RequestHeader("pitch") Float pitch) {
    // TODO: change to actual validation values
    if (roll >= 0 && pitch >= 0) {
      // TODO: call plane system with pitch & roll values
      try {
        sc.setGimbalPosition(roll, pitch);
      } catch (Exception e) {
        logger.info("error... " + e.getMessage());
      }
      return ok().body("Gimbal position changed successfully, roll: " + roll + " pitch: " + pitch);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid roll and pitch values, roll: " + roll + ", pitch: " + pitch);
  }

  /**
   * @return something...
   * calls plane system with new focal length
   * /set-zoom-focal-length, takes json with f32 field “focal_length”
   */
  @RequestMapping(value = "/focal-len", method = RequestMethod.POST)
  public ResponseEntity setFocalLength(@RequestHeader("focalLength") Float focalLength) {
    if (focalLength != null && focalLength >= 0) {
      logger.info("Focal length post request: " + focalLength);
//      /set-zoom-focal-length, takes json with f32 field “focal_length”
      // TODO: call focalLength endpoint thing plane system
      sc.changeFocalLength(focalLength);
      return ok().body("Focal length updated successfully " + focalLength);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid focal length values " + focalLength);
  }

  /**
   * @return something...
   * /set-zoom-level, takes json with u8 field “level” (0-60, any past 30 is digital zoom)
   */
  @RequestMapping(value = "/set-zoom-level", method = RequestMethod.POST)
  public ResponseEntity setZoomLevel (@RequestHeader("level") Float level) {
    if (level >= 0 && level <= 60) {
      logger.info("set zoom level post request: " + level);
      // TODO: call set zoom level endpoint thing plane system
      try {
        sc.changeZoomLevel(level);
      } catch (Exception e) {
        logger.info("error... " + e.getMessage());
      }
      return ok().body("Zoom level updated successfully " + level);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid zoom level input " + level);
  }

  /**
   * @return something...
   * /capture, takes a single image. Don’t call when ps modes are running for now.
   */
  @RequestMapping(value = "/capture", method = RequestMethod.POST)
  public ResponseEntity capture () {
    // if psmodes not running
    logger.info("capture image request");
//  /set-zoom-focal-length, takes json with f32 field “focal_length”
    // TODO: call capture endpoint thing plane system
    sc.capture();
    return ok().body("image finished capturing");
  }

  /**
   * @return something...
   * /get-zoom-level, return the zoom level as an integer (0-60)
   */
  @RequestMapping(value = "/get-zoom-level", method = RequestMethod.GET)
  public ResponseEntity getZoomLevel () {
    logger.info("getting zoom level");
    // TODO: call get zoom level endpoint thing plane system and pass that in to return statement
    try {
      sc.getZoomLevel();
    } catch (Exception e) {
      logger.info("error... " + e.getMessage());
    }
    return ok().body("image finished capturing");
  }

  /*
   * TODO:
   * save focal length - post that gets the focal length and sends setting update to the plane system
   * save gimbal position - post to call ps method - takes roll and pitch as two params
   * save ps mode - post request
   *
   * - /pan-search - starts panning with positions from the ps config file
   * how to end??
- /manual-search  - starts or stops cc
- /distance-search - run distance, still need way to access waypoint list from autopilot to function
- /time-search  - run timed, takes two u64s: inactive time and active time for periods taking/not taking photos
   */
}
