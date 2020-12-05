package org.cuair.ground.clients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.cuair.ground.models.Image;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

/*
 * Client for GCS communications
 */
public class AutopilotClient {

  private AsyncRestTemplate template = new AsyncRestTemplate();

  private String autopilotAddress =
      "http://" + Flags.AUTOPILOT_GROUND_IP + ":" + Flags.AUTOPILOT_GROUND_PORT;

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
    HttpEntity<Map<String, Object>> requestEntity =
        new HttpEntity<Map<String, Object>>(image.getLocations(), headers);
    ListenableFuture<ResponseEntity<String>> coverageFuture =
        template.exchange(coverageURI, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(coverageURI, coverageFuture);
  }
}
