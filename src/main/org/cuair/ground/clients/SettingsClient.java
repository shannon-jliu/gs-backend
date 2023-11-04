package org.cuair.ground.clients;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.coyote.Response;
import org.cuair.ground.controllers.ImageController;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
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

  private String obcAddress = "http://" + Flags.OBC_IP + ":";

  private String psModesAddress = "http://" + Flags.PS_MODES_IP + ":";

  private String gimbalCommandsAddress = "http://" + Flags.GIMBAL_COMMANDS_IP + ":";

  private String cameraCommandsAddress = "http://" + Flags.CAMERA_COMMANDS_IP + ":";

  private String serverPort8080 = "8080"; // TODO: fix later...


  protected String serverPort;
  protected String setModeRoute;

  protected String getModeRoute;
  private String setFocalLengthRoute = Flags.SET_FOCAL_LENGTH_ROUTE;

  private String setZoomLevelRoute = Flags.SET_ZOOM_LEVEL_ROUTE;

  private String setAperatureRoute = Flags.SET_APERTURE_ROUTE;

  private String setShutterSpeed = Flags.SET_SHUTTER_SPEED_ROUTE;

  private String setGimbalRoute = Flags.SET_GIMBAL_ROUTE;

  private String setPanSearchRoute = Flags.SET_PAN_SEARCH_ROUTE;

  private String setManualSearchRoute = Flags.SET_MANUAL_SEARCH_ROUTE;

  private String setDistanceSearchRoute = Flags.SET_DISTANCE_SEARCH_ROUTE;

  private String setTimeSearchRoute = Flags.SET_TIME_SEARCH_ROUTE;

  private String setCaptureRoute = Flags.CAPTURE_ROUTE;

  private String getZoomLevelRoute = Flags.GET_ZOOM_LEVEL_ROUTE;

  /**
   * Changes the mode of the plane server
   *
   * @param setting the new setting to change to
   */
  public void changeMode(T setting) {
    URI settingsURI = URI.create(obcAddress + serverPort + setModeRoute);
    HttpEntity<T> requestEntity = new HttpEntity<T>(setting, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
  }

  /**
   * Gets the mode of the plane server
   */
  public ResponseEntity<String> getMode() throws Exception {
    URI settingsURI = URI.create(obcAddress + serverPort + getModeRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    return settingsFuture.get();
  }

  /**
   * Gets the focal length from the plane server
   */
  public ResponseEntity<String> getFocalLength() throws Exception {
    // switch 0 to the address
    URI settingsURI = URI.create(psModesAddress + serverPort8080 + setFocalLengthRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("works - get focal length...");
    return settingsFuture.get();
  }

  /**
   * Gets the gimbal position from the plane server
   */
  public ResponseEntity<String> getGimbalPosition() throws Exception {
    // !!! not actually implemented on PS side rn - ignore for now
    URI settingsURI = URI.create(psModesAddress + serverPort8080 + "get gimbal route");
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("works - get gimbal pos...");
    return settingsFuture.get();
  }

  /**
   * Gets the gimbal position from the plane server
   */
  public ResponseEntity<String> setGimbalPosition(Float roll, Float pitch) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("pitch", pitch);
    data.put("roll", roll);
    URI settingsURI = URI.create(psModesAddress + serverPort8080 + setGimbalRoute);
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(data, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("works - set gimbal pos...");
    return settingsFuture.get();
  }

  /**
   * Sets the plane system mode as pan search
   */
  public ResponseEntity<String> setPanSearch() throws Exception {
    URI settingsURI = URI.create(psModesAddress + serverPort8080 + setPanSearchRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("works - pan search...");
    return settingsFuture.get();
  }

  /**
   * Sets the plane system mode as manual search
   */
  public ResponseEntity<String> setManualSearch() throws Exception {
    URI settingsURI = URI.create(psModesAddress + serverPort8080 + setManualSearchRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("works - manual search...");
    return settingsFuture.get();
  }

  /**
   * Sets the plane system mode as distance search
   */
  public ResponseEntity<String> setDistanceSearch() throws Exception {
    URI settingsURI = URI.create(psModesAddress + serverPort8080 + setDistanceSearchRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("works - distance search...");
    return settingsFuture.get();
  }

  /**
   * Sets the plane system mode as time search
   */
  public ResponseEntity<String> setTimeSearch(Integer inactive, Integer active) throws Exception {
    URI settingsURI = URI.create(psModesAddress + serverPort8080 + setTimeSearchRoute);
    Map<String, Object> data = new HashMap<>();
    data.put("inactive", inactive);
    data.put("active", active);
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(data, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("works - time search...");
    return settingsFuture.get();
  }

  /**
   * Changes the focal length of the plane server
   *
   * @param focalLength the new focal length to change to
   */
  public ResponseEntity<String> setFocalLength(Float focalLength) throws Exception{
    URI settingsURI = URI.create(cameraCommandsAddress + serverPort8080 + setFocalLengthRoute);
    HttpEntity<Float> requestEntity = new HttpEntity<>(focalLength, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("works - change focaL length... " + focalLength);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Changes the zoom level of the plane server
   *
   * @param level the new zoom level to change to
   */
  public ResponseEntity<String> setZoomLevel(Float level) throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + serverPort8080 + setZoomLevelRoute);
    HttpEntity<Float> requestEntity = new HttpEntity<>(level, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("works - change zoom level... " + level);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Changes the aperture of the plane server
   *
   * @param aperture the new aperture to change to
   */
  public ResponseEntity<String> setAperture(Integer aperture) throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + serverPort8080 + setAperatureRoute);
    HttpEntity<Integer> requestEntity = new HttpEntity<>(aperture, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("works - change aperture... " + aperture);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Changes the shutter speed of the plane server
   *
   * @param numerator to pass into plane system (u16)
   * @param denominator to pass into plane system (u16)
   */
  public ResponseEntity<String> setShutterSpeed(Integer numerator, Integer denominator) throws Exception {
    Map<String, Integer> data = new HashMap<>();
    data.put("numerator", numerator);
    data.put("denominator", denominator);
    URI settingsURI = URI.create(cameraCommandsAddress + serverPort8080 + setShutterSpeed);
    HttpEntity<Map<String, Integer>> requestEntity = new HttpEntity<>(data, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("works - change shutter speed... " + numerator + " " + denominator);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Gets the zoom level of the plane server
   */
  public ResponseEntity<String> getZoomLevel() throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + serverPort8080 + getZoomLevelRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.GET,
        requestEntity, String.class);
    logger.info("works - get zoom level...");
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }

  /**
   * Command to capture an image
   */
  public ResponseEntity<String> capture() throws Exception {
    URI settingsURI = URI.create(cameraCommandsAddress + serverPort8080 + setCaptureRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture = template.exchange(settingsURI, HttpMethod.POST,
        requestEntity, String.class);
    logger.info("works - capture ...");
    RequestUtil.futureCallback(settingsURI, settingsFuture);
    return settingsFuture.get();
  }
}
