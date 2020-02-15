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
import org.cuair.ground.models.Image;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cuair.ground.util.RequestUtil;

/*
 * Client for CGS communications
 */
public class AutopilotClient {

  private AsyncRestTemplate template = new AsyncRestTemplate();

  private String AUTOPILOT_GROUND_IP = Flags.AUTOPILOT_GROUND_IP;

  private String AUTOPILOT_GROUND_PORT = Flags.AUTOPILOT_GROUND_PORT;

  private String AUTOPILOT_COVERAGE = Flags.AUTOPILOT_COVERAGE;

  private String AUTOPILOT_GROUND_MDLC_ROIS = Flags.AUTOPILOT_GROUND_MDLC_ROIS;

  private String OBC_IP = Flags.OBC_IP;

  private String AIRAPI_PORT = Flags.CAM_GIM_PORT;

  private String COVERAGE = Flags.CAM_GIM_MODE;

  public void sendMDLCGroundROIS(JSONObject rois) {
    String roisURL = "http://" + AUTOPILOT_GROUND_IP + ":" + AUTOPILOT_GROUND_PORT + AUTOPILOT_GROUND_MDLC_ROIS;
    URI groundROIs = URI.create(roisURL);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(rois, headers);
    ListenableFuture<ResponseEntity<String>> groundROIfuture = template.exchange(
      groundROIs, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(roisURL, groundROIfuture);
  }

  public void sendCoverageData(Image image) {
    String coverageURL = "http://" + AUTOPILOT_GROUND_IP + ":" + AUTOPILOT_GROUND_PORT + AUTOPILOT_COVERAGE;
    URI coverageURI = URI.create(coverageURL);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    // ObjectNode imageLocations = image.getLocations();
    // HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(imageLocations, headers);
    // ListenableFuture<ResponseEntity<String>> coverageFuture = template.exchange(
    //   coverageURI, HttpMethod.POST, requestEntity, String.class);
    // RequestUtil.futureCallback(coverageURL, coverageFuture);
  }

}
