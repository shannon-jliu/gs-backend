package org.cuair.ground.clients;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.cuair.ground.models.plane.target.Target;
import org.json.JSONObject;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.util.concurrent.ListenableFuture;

/*
 * Client for communication with the judge's Interop server
 * https://github.com/auvsi-suas/interop
 */
public class InteropClient {
  // Template for async client-side http access
  private AsyncRestTemplate template = new AsyncRestTemplate();
  private String sessionCookies = "";

  // Create logger for interop client
  private Logger interopLogger = LoggerFactory.getLogger(InteropClient.class);

  // Constants
  private final String InteropAddress = "http://" + Flags.INTEROP_IP + ":" + Flags.INTEROP_PORT;
  private final String Username = Flags.INTEROP_USERNAME;
  private final String Password = Flags.INTEROP_PASSWORD;
  private final int MissionId = Flags.MISSION_NUMBER;

  // Attempt login on initialization
  public InteropClient() {
    attemptLogin();
  }

  /**
   * Attempts to login to interop.
   */
  private void attemptLogin() {
    interopLogger.info("Attempting login to interop server.");

    // Create URI for interop server location to login
    URI loginLocation = URI.create(InteropAddress + "/api/login");

    // Create Http Headers to pass information (username/password) as json
    // with the login request
    HttpHeaders headers = RequestUtil.getDefaultHeaders();

    // Build the request entity to hold the request
    HttpEntity<String> requestEntity = new HttpEntity<String>(createJsonLogin(), headers);

    // Create listenable future to listen for response of login post request
    ListenableFuture<ResponseEntity<String>> responseFuture = template.exchange(loginLocation,
        HttpMethod.POST,
        requestEntity,
        String.class);

    // Adds callback function to print success or failure when complete
    RequestUtil.futureCallback(loginLocation, responseFuture);

    // Waits until completion of login post request
    // (we need to wait until login is completed before attempting any other
    // requests)
    try {
      ResponseEntity<String> loginResponse = responseFuture.get();
      interopLogger.info("Interop Login Response Obtained");
      updateSessionCookieValue(loginResponse);
      interopLogger.info("Interop Login Cookie: " + sessionCookies);
    } catch (Exception e) {
      interopLogger.error("Failed to receive response from interop login.");
    }
  }

  /**
   * Updates the sessionCookies field of the interop client so that all future
   * requests will be
   * authenticated with the same session as the original login request.
   *
   * @param loginResponse : response entity to the login http request; this
   *                      response entity contains
   *                      a SET_COOKIE field that defines the session id created
   *                      in response to login.
   */
  private void updateSessionCookieValue(ResponseEntity<String> loginResponse) {
    // Get response headers and extract the SET_COOKIE header value
    HttpHeaders responseHeaders = loginResponse.getHeaders();
    String headerSetCookie = responseHeaders.getFirst(HttpHeaders.SET_COOKIE);

    if (headerSetCookie == null) {
      interopLogger.error("Interop login: received no cookies.");
    } else {
      // Extract the value corresponding to the session id
      String sessionId = "sessionid=";
      int semicolonPos = headerSetCookie.indexOf(";"); // session id ends with a semicolon
      sessionCookies = headerSetCookie.substring(sessionId.length(), semicolonPos);
    }
  }

  /**
   * Returns a string containing the json for login.
   */
  private String createJsonLogin() {
    JSONObject login = new JSONObject();
    login.put("username", Username);
    login.put("password", Password);
    return login.toString();
  }

  /**
   * Performs a get request to the specified URI
   *
   * @param uri The URI that is the target of the request
   * @return a ListenableFuture of the get request to interop
   */
  private ListenableFuture<ResponseEntity<String>> getInterop(URI uri) {
    // Create Http Headers with session cookies to stay authenticated
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(sessionCookies);

    // Build the request entity to hold the request
    // (no body as we simply want to receive mission data)
    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

    ListenableFuture<ResponseEntity<String>> responseFuture = template.exchange(uri, HttpMethod.GET, requestEntity,
        String.class);

    RequestUtil.futureCallback(uri, responseFuture);

    return responseFuture;
  }

  /**
   * Performs post/put requests to a specified uri
   *
   * @param uri  the URI to direct a request to
   * @param body the body of the post/put request
   * @param post True if post request, False is put request
   * @return Listenable Future of the response from request
   */
  private ListenableFuture<ResponseEntity<String>> postPutInterop(URI uri, String body, Boolean post) {
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(sessionCookies);

    HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);

    // Create listenable future to listen for response
    // Request method is either POST on creation or PUT for updating
    ListenableFuture<ResponseEntity<String>> responseFuture = template.exchange(uri,
        post ? HttpMethod.POST : HttpMethod.PUT,
        requestEntity,
        String.class);
    RequestUtil.futureCallback(uri, responseFuture);

    return responseFuture;
  }

  /**
   * Get mission data from interop.
   */
  public JSONObject getMissionData() {
    // Create URI for mission information route
    URI missionLocation = URI.create(InteropAddress + "/api/missions/" + MissionId);
    // Get mission data from interop

    JSONObject missionResponse = null;

    // Waits until response is received before packing dictionary with important
    // values
    try {
      interopLogger.info("Mission Data Obtained");
      missionResponse = new JSONObject(getInterop(missionLocation).get().getBody());
      // interopLogger.info(missionResponse.toString());
    } catch (Exception e) {
      interopLogger.error("Failed to receive response from get mission request.");
    }
    return missionResponse;
  }

  /**
   * Helper function to send a target to interop.
   *
   * @param target   - the target we need to send
   * @param creation - true if we are creating the target (sending it to interop
   *                 for the first time)
   *                 and false if we are updating a target we previously send to
   *                 interop
   */
  private ListenableFuture<ResponseEntity<String>> sendTarget(Target target, boolean creation) {
    // Build the target route
    String targetRouteString = InteropAddress + "/api/odlcs";
    if (!creation) {
      // If we are not creating a target, we need to specify the id in the target
      // route
      targetRouteString += "/" + Long.toString(target.getJudgeTargetId());
    }
    URI targetRoute = URI.create(targetRouteString);
    // Generate the body from the target we wish to either update or post to interop
    String body = target.toInteropJson().toString();
    // update/post target to interop
    return postPutInterop(targetRoute, body, creation);
  }

  /**
   * Performs a post request to add a new specified target to interop
   *
   * @param target The target being added to interop
   * @return The response for this get request (the accepted json object)
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public ListenableFuture<ResponseEntity<String>> createTarget(Target target)
      throws ExecutionException, InterruptedException {
    interopLogger.info(target.toInteropJson().toString());
    ListenableFuture<ResponseEntity<String>> response = sendTarget(target, true);
    // parse the response
    JSONObject jsonResp = new JSONObject(response.get().getBody());
    interopLogger.info("Response to target submission:" + jsonResp.toString());
    Long l = (long) (int) jsonResp.get("id");
    interopLogger.info("id: " + l);

    // update the target to have the judge id so we can perform updates later
    target.setJudgeTargetId_CREATION(l);
    return response;
  }

  /**
   * Performs a post request to update a specified target
   *
   * @param target The target being updated
   */
  public void updateTarget(Target target)
      throws ExecutionException, InterruptedException {
    ListenableFuture<ResponseEntity<String>> response = sendTarget(target, false);
    // parse the response
    JSONObject jsonResp = new JSONObject(response.get().getBody());
    interopLogger.info("Response to target update:" + jsonResp.toString());
  }

  public void sendThumbnail(byte[] imageContent, Target target) {
    interopLogger.info("Attempting to submit thumbnail for target with id " + target.getJudgeTargetId());

    // Build the thumbnail route
    URI thumbnailRoute = URI.create(InteropAddress + "/api/odlcs/" + target.getJudgeTargetId() + "/image");

    // Create Http Headers with session cookies to stay authenticated
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(sessionCookies);

    // Build the request entity with the map content
    HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageContent, headers);

    // Create listenable future to listen for response
    ListenableFuture<ResponseEntity<String>> responseFuture = template.exchange(thumbnailRoute,
        HttpMethod.PUT,
        requestEntity,
        String.class);

    RequestUtil.futureCallback(thumbnailRoute, responseFuture);
  }

  /**
   * Get sent targets from interop
   *
   * @return ListenableFuture response that includes information of first 100 sent
   *         targets from interop
   */
  public ListenableFuture<ResponseEntity<String>> getSentTargets() {
    // URI for the target locations
    URI getTargetLocation = URI.create(InteropAddress + "/api/odlcs");
    // return the request to get targets
    return getInterop(getTargetLocation);
  }

  /**
   * Get sent target from interop by id
   *
   * @param id The id of the target you are trying to recieve
   * @return ListenableFuture which includes information on particular odlc
   */
  public ListenableFuture<ResponseEntity<String>> getSentTarget(long id) {
    URI getTargetByID = URI.create(InteropAddress + "/api/odlcs/" + id);
    return getInterop(getTargetByID);
  }

  /**
   * Sends a map to interop.
   * 
   * @param imageContent - raw binary content of map image
   * @return true iff submission to interop succeeded
   */
  public boolean sendMapping(byte[] imageContent) {
    // Build the map route
    URI mapRoute = URI.create(InteropAddress + "/api/maps/" + MissionId + "/" + Username);

    // Create Http Headers with session cookies to stay authenticated
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(sessionCookies);

    // Build the request entity with the map content
    HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageContent, headers);

    // Create listenable future to listen for response
    ListenableFuture<ResponseEntity<String>> responseFuture = template.exchange(mapRoute,
        HttpMethod.PUT,
        requestEntity,
        String.class);

    RequestUtil.futureCallback(mapRoute, responseFuture);

    // Checks for successful submission
    try {
      responseFuture.get();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}