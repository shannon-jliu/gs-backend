package org.cuair.ground.clients;

import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import java.net.URI;
import org.json.*;
import org.cuair.ground.protobuf.InteropApi.*;
import com.google.protobuf.util.JsonFormat; 

/*
 * Client for interop communications
 */
public class InteropClient {

  private AsyncRestTemplate template = new AsyncRestTemplate();

  public String INTEROP_IP = "localhost";

  public String INTEROP_PORT = "8000";

  public String LOGIN = "/api/login";

  public String USERNAME = "testadmin";

  public String PASSWORD = "testpass";

  public void attemptLogin() {

    URI interopURI = URI.create("http://" + INTEROP_IP + ":" + INTEROP_PORT + LOGIN);

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    Credentials credentials = Credentials.newBuilder()
      .setUsername(USERNAME)
      .setPassword(PASSWORD)
      .build();

    // todo with protobuf 
    JSONObject personJsonObject = new JSONObject();
    personJsonObject.put("username", USERNAME);
    personJsonObject.put("password", PASSWORD);

    try {

      HttpEntity<String> requestEntity = new HttpEntity<String>(JsonFormat.printer().print(credentials), headers);
      ListenableFuture<ResponseEntity<String>> future1 = template.exchange(
        interopURI, HttpMethod.POST, requestEntity, String.class);

      future1.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

          @Override
          public void onSuccess(ResponseEntity<String> result) {
            System.out.println("on success");
              // todo
          }

          @Override
          public void onFailure(Throwable ex) {
              // todo
            System.out.println("on failure");
          }

      });
    } catch (Exception e) {}
  }

}
