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
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;

/*
 * Client for CGS communications
 */
public class CameraGimbalClient {

  private AsyncRestTemplate template = new AsyncRestTemplate();

  private String OBC_IP = Flags.OBC_IP;

  private String CAM_GIM_PORT = Flags.CAM_GIM_PORT;

  private String CAM_GIM_MODE = Flags.CAM_GIM_MODE;

  public void changeCamGimMode(CameraGimbalSettings.CameraGimbalMode camGimMode) {
    String camGimModeURL = "http://" + OBC_IP + ":" + CAM_GIM_PORT + CAM_GIM_MODE;

    URI cgsModeURI = URI.create(camGimModeURL);

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    CameraGimbalSettings cgs = new CameraGimbalSettings(camGimMode);
    HttpEntity<CameraGimbalSettings> requestEntity = new HttpEntity<CameraGimbalSettings>(cgs, headers);
    ListenableFuture<ResponseEntity<String>> csgModeFuture = template.exchange(
      cgsModeURI, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(camGimModeURL, csgModeFuture);

  }

}
