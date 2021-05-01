package org.cuair.ground.clients;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.tomcat.util.codec.binary.Base64;
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
  Logger interopLogger = LoggerFactory.getLogger(InteropClient.class);


  // Constants
  private final String InteropAddress = 
    "http://" + Flags.INTEROP_IP + ":" + Flags.INTEROP_PORT;
  private final String Username = Flags.INTEROP_USERNAME;
  private final String Password = Flags.INTEROP_PASSWORD;
  private final String MissionId = "1";

  // Attempt login on initialization
  public InteropClient() {
    attemptLogin();
  }

  /**
   * Attempts to login to interop.
   */
  private void attemptLogin() {
    // Create URI for interop server location to login
    URI loginLocation = URI.create(InteropAddress + "/api/login");

    // Create Http Headers to pass information (username/password) as json
    // with the login request
    HttpHeaders headers = RequestUtil.getDefaultHeaders();

    // Build the request entity to hold the request
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(createJsonLogin(), headers);
    
    // Create listenable future to listen for response of login post request
    ListenableFuture<ResponseEntity<String>> responseFuture =
      template.exchange(loginLocation,
                        HttpMethod.POST,
                        requestEntity, 
                        String.class);

    // Adds callback function to print success or failure when complete
    RequestUtil.futureCallback(loginLocation, responseFuture);

    // Waits until completion of login post request
    // (we need to wait until login is completed before attempting any other requests)
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
   * Updates the sessionCookies field of the interop client so that all future requests will be
   * authenticated with the same session as the original login request.
   * @param loginResponse : response entity to the login http request; this response entity contains
   * a SET_COOKIE field that defines the session id created in response to login.
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
   * Get mission data from interop.
   */
  public void getMissionData() {
    // Create URI for mission information route
    URI missionLocation = URI.create(InteropAddress + "/api/missions/" + MissionId);

    // Create Http Headers to receive mission data in json format
    // Need to have cookies from session
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(sessionCookies);

    // Build the request entity to hold the request
    // (no body as we simply want to receive mission data)
    HttpEntity<String> requestEntity =
        new HttpEntity<String>(headers);

    // Create listenable future to listen for response of login post request
    ListenableFuture<ResponseEntity<String>> responseFuture =
        template.exchange(missionLocation, HttpMethod.GET, requestEntity, String.class);

    RequestUtil.futureCallback(missionLocation, responseFuture);
  }
}