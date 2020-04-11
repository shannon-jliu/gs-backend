package org.cuair.ground.clients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.plane.settings.*;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
import org.json.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

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

  private String COVERAGE = Flags.AUTOPILOT_COVERAGE;

  public void sendMDLCGroundROIS(JSONObject rois) {
    URI groundROIs =
        URI.create(
            "http://"
                + AUTOPILOT_GROUND_IP
                + ":"
                + AUTOPILOT_GROUND_PORT
                + AUTOPILOT_GROUND_MDLC_ROIS);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(rois, headers);
    ListenableFuture<ResponseEntity<String>> groundROIfuture =
        template.exchange(groundROIs, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(groundROIs, groundROIfuture);
  }

  public void sendCoverageData(Image image) {
    URI coverageURI =
        URI.create(
            "http://" + AUTOPILOT_GROUND_IP + ":" + AUTOPILOT_GROUND_PORT + AUTOPILOT_COVERAGE);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ObjectNode> requestEntity =
        new HttpEntity<ObjectNode>(/* TODO image.getLocations()*/"", headers);
    ListenableFuture<ResponseEntity<String>> coverageFuture =
        template.exchange(coverageURI, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(coverageURI, coverageFuture);
  }
}