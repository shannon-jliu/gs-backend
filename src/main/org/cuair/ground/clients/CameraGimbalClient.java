package org.cuair.ground.clients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.List;
import java.util.Map;
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

  private String planeServerAddress = "http://localhost:" + this.serverPort;

  /**
   * Client to communicate with camera gimbal server
   */
  public CameraGimbalClient() {
    this.serverPort = Flags.CAM_GIM_PORT;
    this.getModeRoute = Flags.GET_CAM_GIM_MODE_SETTINGS_ROUTE;
  }

  /**
   * Sends MDLC ROI's to the plane system
   *
   * @param rois the JSON representation of the ROIS
   */
  public void sendMDLCGroundROIS(List<ROI> rois) {
    URI groundROIs = URI.create(planeServerAddress);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<List<ROI>> requestEntity = new HttpEntity<List<ROI>>(rois, headers);
    ListenableFuture<ResponseEntity<String>> groundROIfuture =
        template.exchange(groundROIs, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(groundROIs, groundROIfuture);
  }

}
