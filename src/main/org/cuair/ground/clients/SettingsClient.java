package org.cuair.ground.clients;

import java.net.URI;
import org.cuair.ground.models.plane.settings.*;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
import org.json.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

/*
 * Client for settings communications with plane servers
 */
public class SettingsClient<T> {

  private AsyncRestTemplate template = new AsyncRestTemplate();

  private String OBC_ADDRESS = "http://" + Flags.OBC_IP + ":";

  protected String SERVER_PORT;

  protected String SET_MODE_ROUTE;

  protected String GET_MODE_ROUTE;

  public void changeMode(T setting) {
    URI settingsURI = URI.create(OBC_ADDRESS + SERVER_PORT + SET_MODE_ROUTE);
    HttpEntity<T> requestEntity = new HttpEntity<T>(setting, RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture =
        template.exchange(settingsURI, HttpMethod.POST, requestEntity, String.class);
    RequestUtil.futureCallback(settingsURI, settingsFuture);
  }

  public ResponseEntity<String> getMode() throws Exception {
    URI settingsURI = URI.create(OBC_ADDRESS + SERVER_PORT + GET_MODE_ROUTE);
    HttpEntity<String> requestEntity = new HttpEntity<String>(RequestUtil.getDefaultHeaders());
    ListenableFuture<ResponseEntity<String>> settingsFuture =
        template.exchange(settingsURI, HttpMethod.GET, requestEntity, String.class);
    return settingsFuture.get();
  }
}