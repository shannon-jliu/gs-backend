package org.cuair.ground.clients;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.coyote.Response;
import org.cuair.ground.controllers.ImageController;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

/**
 * Client for settings communications with plane servers
 */
public class SettingsClient<T> {

  private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

  private AsyncRestTemplate template = new AsyncRestTemplate();

  private final String obcAddress = "http://" + Flags.OBC_IP + ":";

  private final String cameraCommandsAddress = "http://" + Flags.CAMERA_COMMANDS_IP + ":";
  protected String serverPort;

  // TODO: combine maybe into one string of address + port bc now all the same
  // port
  private final String cameraCommandsPort = Flags.MAIN_CAMERA_COMMANDS_PORT;
  private String setModeRoute;

  // Note: the protected syntax (like for getModeRoute) doesn't really work - is
  // null somehow ?
  // for now, just use private final String _ = Flags._ instead
  protected String getModeRoute;
  private final String setFocalLengthRoute = Flags.SET_FOCAL_LENGTH_ROUTE;

  private final String setZoomLevelRoute = Flags.SET_ZOOM_LEVEL_ROUTE;

  private final String setApertureRoute = Flags.SET_APERTURE_ROUTE;

  private final String setShutterSpeed = Flags.SET_SHUTTER_SPEED_ROUTE;

  private final String controlGimbalRoute = Flags.CONTROL_GIMBAL_ROUTE;

  private final String setExposureModeRoute = Flags.SET_EXPOSURE_MODE_ROUTE;

  private final String setPanSearchRoute = Flags.SET_PAN_SEARCH_ROUTE;

  private final String setManualSearchRoute = Flags.SET_MANUAL_SEARCH_ROUTE;

  private final String setDistanceSearchRoute = Flags.SET_DISTANCE_SEARCH_ROUTE;

  private final String setTimeSearchRoute = Flags.SET_TIME_SEARCH_ROUTE;

  private final String setCaptureRoute = Flags.CAPTURE_ROUTE;

  private final String getStatusRoute = Flags.GET_STATUS_ROUTE;

  /**
   * Command to capture an image
   */
  public ResponseEntity<String> capture() throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setCaptureRoute);
    logger.info(settingsURI.toString());
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("Capture command called in SettingsClient");
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Gets the status from the plane system
   * Returns json with ints: “shutter_speed_num” and “shutter_speed_den”,
   * “aperture”, and “iso,”
   * and strings: “exposure_mode” and “focus_mode”
   */
  public ResponseEntity<String> getStatus() throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + getStatusRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("Get status called");
    return settingsFuture.get();
  }

  /**
   * Sets the plane system mode as pan search
   */
  public ResponseEntity<String> setPanSearch() throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setPanSearchRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("Set to pan search mode called");
    return settingsFuture.get();
  }

  /**
   * Sets the plane system mode as manual search
   */
  public ResponseEntity<String> setManualSearch() throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setManualSearchRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("Set to manual search called");
    return settingsFuture.get();
  }

  /**
   * Sets the plane system mode as distance search
   */
  public ResponseEntity<String> setDistanceSearch() throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setDistanceSearchRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("Set to distance search called");
    return settingsFuture.get();
  }

  /**
   * Sets the plane system mode as time search
   *
   * @param inactive integer for period of not taking photos
   * @param active   integer for period of taking photos
   */
  public ResponseEntity<String> setTimeSearch(Integer inactive, Integer active) throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setTimeSearchRoute);
    JSONObject json = new JSONObject();
    json.put("inactive", inactive);
    json.put("active", active);
    HttpEntity<String> requestEntity = new HttpEntity<>(json.toString(), RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("Set to time search called with inactive = " + inactive + " and active = " + active);
    return settingsFuture.get();
  }

  /**
   * Sets the gimbal position - sends roll and pitch to the plane server
   */
  public ResponseEntity<String> controlGimbal(Float roll, Float pitch) throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + controlGimbalRoute);
    // Creating a new JSON object to send to PS with body {"pitch": _, "roll": }
    // HTTP request contains this JSON object and default headers with contentType =
    // APPLICATION_JSON
    JSONObject json = new JSONObject();
    json.put("pitch", pitch);
    json.put("roll", roll);
    HttpEntity<String> requestEntity = new HttpEntity<>(json.toString(), RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("Set gimbal position called");
    return settingsFuture.get();
  }

  /**
   * Changes the focal length of the plane server
   *
   * @param focalLength the new focal length to change to
   */
  public ResponseEntity<String> setFocalLength(Float focalLength) throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setFocalLengthRoute);
    JSONObject json = new JSONObject();
    json.put("focalLength", focalLength);
    HttpEntity<String> requestEntity = new HttpEntity<>(json.toString(), RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("Changing focal length to " + focalLength);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Changes the zoom level of the plane server
   *
   * @param level the new zoom level to change to
   */
  public ResponseEntity<String> setZoomLevel(Integer level) throws Exception {
    // can use either map or json object
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setZoomLevelRoute);
    Map<String, Integer> json = new HashMap<>();
    json.put("level", level);
    HttpEntity<Map<String, Integer>> requestEntity = new HttpEntity<>(json, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("req entity " + String.valueOf(requestEntity));
    logger.info("Changing zoom level to " + level);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Changes the aperture of the plane server
   *
   * @param aperture the new aperture to change to
   */
  public ResponseEntity<String> setAperture(Integer aperture) throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setApertureRoute);
    JSONObject json = new JSONObject();
    json.put("aperture", aperture);
    HttpEntity<String> requestEntity = new HttpEntity<>(json.toString(), RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("Changing aperture to " + aperture);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Changes the shutter speed of the plane server
   *
   * @param numerator   to pass into plane system (u16)
   * @param denominator to pass into plane system (u16)
   */
  public ResponseEntity<String> setShutterSpeed(Integer numerator, Integer denominator) throws Exception {
    JSONObject json = new JSONObject();
    json.put("numerator", numerator);
    json.put("denominator", denominator);
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setShutterSpeed);
    HttpEntity<String> requestEntity = new HttpEntity<>(json.toString(), RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("Changing shutter speed with numerator = " + numerator + " denominator = " + denominator);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Changes the exposure mode of the camera
   * 
   * @param mode to pass into plane system
   */
  public ResponseEntity<String> setExposureMode(String mode) throws Exception {
    JSONObject json = new JSONObject();
    json.put("mode", mode);
    URI settingsURI = URI.create(cameraCommandsAddress + cameraCommandsPort + setExposureModeRoute);
    HttpEntity<String> requestEntity = new HttpEntity<>(json.toString(), RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("Changing exposure mode to " + mode);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }
}
