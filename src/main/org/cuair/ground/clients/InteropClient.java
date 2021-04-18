package org.cuair.ground.clients;

import java.net.URI;
import org.json.JSONObject;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
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

  
  private final String InteropAddress = 
    "http://" + Flags.INTEROP_IP + ":" + Flags.INTEROP_PORT;
  private final String Username = Flags.INTEROP_USERNAME;
  private final String Password = Flags.INTEROP_PASSWORD;
  private final String MissionId = "1";

  public InteropClient() {
    // Nothing here yet
  }

  /**
   * Attempts to login to interop.
   */
  public void attemptLogin() {
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
    
    // Print out success or failure when complete
    // Status code 200 -> OK, not 200 -> something happened (interop readme)
    RequestUtil.futureCallback(loginLocation, responseFuture);
  }

  /**
   * Get mission data from interop.
   */
  public void getMissionData() {
    // Create URI for mission information route
    URI missionLocation = URI.create(InteropAddress + "/api/missions/" + MissionId);

    // Create Http Headers to receive mission data in json format
    HttpHeaders headers = RequestUtil.getDefaultHeaders();

    // Build the request entity to hold the request
    // (no body as we simply want to receive mission data)
    HttpEntity<String> requestEntity =
        new HttpEntity<String>(headers);

    // Create listenable future to listen for response of login post request
    ListenableFuture<ResponseEntity<String>> responseFuture =
        template.getForEntity(missionLocation, String.class);

    RequestUtil.futureCallback(missionLocation, responseFuture);
  }

  private String createJsonLogin() {
    JSONObject login = new JSONObject();
    login.put("username", Username);
    login.put("password", Password);
    return login.toString();
  }
}