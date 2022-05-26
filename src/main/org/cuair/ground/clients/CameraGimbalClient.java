package org.cuair.ground.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.cuair.ground.models.Image;
import org.cuair.ground.models.ROI;
import org.cuair.ground.models.plane.settings.CameraGimbalSettings;
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

/**
 * Client for CGS communications
 */
public class CameraGimbalClient extends SettingsClient<CameraGimbalSettings> {

  private AsyncRestTemplate template = new AsyncRestTemplate();

  private String planeServerAddress;

  /**
   * Client to communicate with camera gimbal server
   */
  public CameraGimbalClient() {
    this.serverPort = Flags.CAM_GIM_PORT;
    this.setModeRoute = Flags.SET_CAM_GIM_MODE_SETTINGS_ROUTE;
    this.getModeRoute = Flags.GET_CAM_GIM_MODE_SETTINGS_ROUTE;
    planeServerAddress = "http://localhost:" + this.serverPort + "/";
  }


  public ObjectNode roisToJson(List<ROI> rois){
    ObjectNode on  = new ObjectMapper().createObjectNode();
    ArrayNode arrayNode = on.putArray("rois");
    for (ROI roi :rois) {
      arrayNode.add(roi.toJson());
    }
    on.put("client_type", "mdlc");
    return on;
  }

  /**
   * Sends MDLC ROI's to the plane system
   *
   * @param rois the JSON representation of the ROIS
   */
  public ListenableFuture<ResponseEntity<String>> sendMDLCGroundROIS(List<ROI> rois) {
    URI groundROIs = URI.create(planeServerAddress+"api/rois");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ObjectNode on = roisToJson(rois);

    HttpEntity<String> requestEntity = new HttpEntity<>(on.toString(), headers);
    ListenableFuture<ResponseEntity<String>> groundROIfuture =
        template.exchange(groundROIs, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(groundROIs, groundROIfuture);
    return groundROIfuture;
  }

  /**
   * Gets the ids of all registered ROIs on the plane system
   */
  public ResponseEntity<String> getRegisteredROIs() throws Exception{
    URI planeRegisteredROIs = URI.create(planeServerAddress+"api/rois");
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> roiFuture =
        template.exchange(planeRegisteredROIs, HttpMethod.GET, requestEntity, String.class);
    return roiFuture.get();
  }

  /**
   * Gets the ids of all captured ROIs on the plane system
   */
  public ResponseEntity<String> getCapturedROIs() throws Exception{
    URI planeCapturedROIs = URI.create(planeServerAddress+"api/captured-rois");
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> roiFuture =
        template.exchange(planeCapturedROIs, HttpMethod.GET, requestEntity, String.class);
    return roiFuture.get();
  }

  /**
   * Gets the capture plan for ROIs from the plane system
   */
  public ResponseEntity<String> getCapturePlan() throws Exception{
    URI capturePlan = URI.create(planeServerAddress+"api/capture-plan");
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> captureFuture =
        template.exchange(capturePlan, HttpMethod.GET, requestEntity, String.class);
    return captureFuture.get();
  }
}
