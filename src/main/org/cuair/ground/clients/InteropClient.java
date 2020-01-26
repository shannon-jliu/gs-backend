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

    // todo with protobuf 
    JSONObject personJsonObject = new JSONObject();
    personJsonObject.put("username", USERNAME);
    personJsonObject.put("password", PASSWORD);

    HttpEntity<String> requestEntity = new HttpEntity<String>(personJsonObject.toString(), headers);
    ListenableFuture<ResponseEntity<String>> future1 = template.exchange(
      interopURI, HttpMethod.POST, requestEntity, String.class);

    future1.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

        @Override
        public void onSuccess(ResponseEntity<String> result) {
            // todo
        }

        @Override
        public void onFailure(Throwable ex) {
            // todo
        }

    });
  }

}
