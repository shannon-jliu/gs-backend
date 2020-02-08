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
import com.google.protobuf.util.JsonFormat; 
import org.cuair.ground.models.plane.settings.*;

/*
 * Client for CGS communications
 */
public class CameraGimbalClient {

  private AsyncRestTemplate template = new AsyncRestTemplate();

  private String OBC_IP = "192.168.0.21";

  private String OBC_PORT = "5005";

  private String CGS_MODE_ROUTE = "/api/mode";

  public void changeCamGimMode() {

    URI cgsModeURI = URI.create("http://" + OBC_IP + ":" + OBC_PORT + CGS_MODE_ROUTE);

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    try {
      CameraGimbalSettings cgs = new CameraGimbalSettings(CameraGimbalSettings.CameraGimbalMode.FIXED);

      HttpEntity<CameraGimbalSettings> requestEntity = new HttpEntity<CameraGimbalSettings>(cgs, headers);
      ListenableFuture<ResponseEntity<String>> future1 = template.exchange(
        cgsModeURI, HttpMethod.POST, requestEntity, String.class);

      future1.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

          @Override
          public void onSuccess(ResponseEntity<String> result) {
            System.out.println("cam on success");
              // todo
          }

          @Override
          public void onFailure(Throwable ex) {
              // todo
            System.out.println("cam on failure");
          }

      });
    } catch (Exception e) {

    }
  }



}
