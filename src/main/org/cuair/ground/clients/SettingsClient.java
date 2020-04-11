package org.cuair.ground.clients;

import java.net.URI;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
import org.json.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

/**
 * Client for settings communications with plane servers
 */
public class SettingsClient<T> {

  private AsyncRestTemplate template = new AsyncRestTemplate();

  private String obcAddress = "http://" + Flags.OBC_IP + ":";

  protected String serverPort;

  protected String setModeRoute;

  protected String getModeRoute;

  /** 
   * Changes the mode of the plane server
   * 
   * @param setting the new setting to change to
   */
  public void changeMode(T setting) {
    URI settingsURI = URI.create(obcAddress + serverPort + setModeRoute);
    HttpEntity<T> requestEntity = new HttpEntity<T>(setting, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture =
        template.exchange(settingsURI, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
  }

  /** 
   * Gets the mode of the plane server
   */
  public ResponseEntity<String> getMode() throws Exception {
    URI settingsURI = URI.create(obcAddress + serverPort + getModeRoute);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture =
        template.exchange(settingsURI, HttpMethod.GET, requestEntity, String.class);
    return settingsFuture.get();
  }
}
