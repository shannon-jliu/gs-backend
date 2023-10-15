package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.ok;
import org.cuair.ground.clients.CameraGimbalClient;
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

  /**
   * @return something...
   * calls plane system with new focal length
   */
  @RequestMapping(value = "/focal-len", method = RequestMethod.POST)
  public ResponseEntity focalLengthUpdate(@RequestHeader("focalLength") Float focalLength) {
    if (focalLength != null && focalLength >= 0) {
      logger.info("Focal length post request: " + focalLength);
      // TODO: call focalLength endpoint thing plane system
      return ok().body("Focal length updated successfully " + focalLength);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid focal length values " + focalLength);
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
      return ok().body("Gimbal position changed successfully, roll: " + roll + " pitch: " + pitch);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid roll and pitch values, roll: " + roll + ", pitch: " + pitch);
  }

  /**
   * @return
   * changes the ps mode to pan-search
   */
  @RequestMapping(value = "/pan-search", method = RequestMethod.POST)
  public ResponseEntity startPanSearch() {
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
    return ok().body("toggled manual search");
  }

  /**
   * @return
   * changes the ps mode to distance-search
   */
  @RequestMapping(value = "/distance-search", method = RequestMethod.POST)
  public ResponseEntity startDistanceSearch() {
    // TODO: add actual toggle call
    return ok().body("running distance search");
  }

  /**
   * @return
   * changes the ps mode to distance-search
   */
  @RequestMapping(value = "/time-search", method = RequestMethod.POST)
  public ResponseEntity startDistanceSearch(@RequestHeader("inactiveTime") Float inactiveTime, @RequestHeader("activeTime") Float activeTime) {
    // TODO: add actual toggle call
    if (activeTime >= 0 && inactiveTime >= 0) {
      return ok().body("running time search");
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, invalid inactive & active times, inactive time: " + inactiveTime + ", active time: " + activeTime);
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
