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

  public InteropClient() {
    // Nothing here yet
  }

  public void attemptLogin() {
    // Create URI for interop server location
    URI interopLocation = URI.create(InteropAddress);

    // Create Http Headers to pass information (username/password) as json
    // with the login request
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Build the request entity to hold the request
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(createJsonLogin(), headers);
    
    // Create listenable future to listen for response of login post request
    ListenableFuture<ResponseEntity<String>> responseFuture =
      template.exchange(interopLocation, 
                        HttpMethod.POST, 
                        requestEntity, 
                        String.class);
    
    // Print out success or failure when complete
    // Status code 200 -> OK, not 200 -> something happened (interop readme)
    RequestUtil.futureCallback(interopLocation, responseFuture);
  }

  private String createJsonLogin() {
    JSONObject login = new JSONObject();
    json.put("password", Password);
    json.put("username", Username);
    return login.toString();
  }
}