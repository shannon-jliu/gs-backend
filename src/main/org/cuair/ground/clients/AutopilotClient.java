package org.cuair.ground.clients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import org.cuair.ground.models.Image;
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

  private String autopilotAddress = "http://" + Flags.AUTOPILOT_GROUND_IP + ":" + Flags.AUTOPILOT_GROUND_PORT;

  /** 
   * Sends MDLC ROI's to autopilot ground station.
   * 
   * @param rois the JSON representation of the ROIS
   */
  public void sendMDLCGroundROIS(JSONObject rois) {
    URI groundROIs =
        URI.create(autopilotAddress + Flags.AUTOPILOT_GROUND_MDLC_ROIS);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(rois, headers);
    ListenableFuture<ResponseEntity<String>> groundROIfuture =
        template.exchange(groundROIs, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(groundROIs, groundROIfuture);
  }

  /** 
   * Sends coverage to autopilot ground station of the specified image.
   * 
   * @param image the image object to send coverage about
   */
  public void sendCoverageData(Image image) {
    URI coverageURI =
        URI.create(autopilotAddress + Flags.AUTOPILOT_COVERAGE);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ObjectNode> requestEntity =
        new HttpEntity<ObjectNode>(image.getLocations(), headers);
    ListenableFuture<ResponseEntity<String>> coverageFuture =
        template.exchange(coverageURI, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(coverageURI, coverageFuture);
  }
}