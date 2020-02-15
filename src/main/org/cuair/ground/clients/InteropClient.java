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
import org.cuair.ground.protobuf.InteropApi.*;
import com.google.protobuf.util.JsonFormat; 
import org.cuair.ground.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.HttpCookie;

/*
 * Client for interop communications
 */
public class InteropClient {

  private static final Logger logger = LoggerFactory.getLogger(InteropClient.class);

  private AsyncRestTemplate template = new AsyncRestTemplate();

  public String INTEROP_IP = "localhost";

  public String INTEROP_PORT = "8000";

  public String LOGIN = "/api/login";

  public String USERNAME = "testadmin";

  public String PASSWORD = "testpass";

  public String MISSION_INFO = Flags.MISSION_INFO;

  public String cookieValue = "";

  public void processOffAxisPos(Mission missionInfo) {
    Position offAxisPos = missionInfo.getOffAxisOdlcPos();
    double latitude = offAxisPos.getLatitude();
    double longitude = offAxisPos.getLongitude();
    double altitude = offAxisPos.getAltitude();
  }

  public void processEmergent(Mission missionInfo) {
    Position emergentPos = missionInfo.getEmergentLastKnownPos();
    double latitude = emergentPos.getLatitude();
    double longitude = emergentPos.getLongitude();
    double altitude = emergentPos.getAltitude();
  }

  public void processAirdrop(Mission missionInfo) {

  }

  public void getMissionInfo() {
    String missionURL = "http://" + INTEROP_IP + ":" + INTEROP_PORT + MISSION_INFO;
    System.out.println(cookieValue);
    URI missionURI = URI.create(missionURL);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", String.format("sessionid=%s", cookieValue));
    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
    ListenableFuture<ResponseEntity<String>> missionFuture = template.exchange(
        missionURI, HttpMethod.GET, requestEntity, String.class);

    // todo printing error messages when success callback is bad
    RequestUtil.SuccessCallback<String> missionCallback = (ResponseEntity<String> result)-> {
      System.out.println("HERE");
      Mission.Builder missionBuilder = Mission.newBuilder();
      //System.out.println(Mission.newBuilder().getClass());
      
      //System.out.println(result.getBody());
      try {
        //int idd = result.getBody().getId();
        //System.out.println(idd + " fs");
        JsonFormat.parser().merge(result.getBody().substring(1, result.getBody().length()), missionBuilder);
        System.out.println("heree");
        Mission missionInfo = missionBuilder.build();
        System.out.println(missionInfo.getOffAxisOdlcPos().getLatitude());
      } catch (Exception e) { // todo specific exception
        System.out.println("ERROR " + e.getMessage());
      }
      System.out.println("did this just die");

      // init emergent
      // init off axis
      // init airdrop
    };


    RequestUtil.futureCallback(missionURL, missionFuture, missionCallback);
  }

  public Odlc createOdlcProto(Target target) {
    Odlc.Builder odlcProto = Odlc.newBuilder();
    if (target.)



    if (null != target.geotag?.gpsLocation) {
      odlcProto
        .setLatitude(target.geotag.gpsLocation.latitude)
        .setLongitude(target.geotag.gpsLocation.longitude)
    } else {
      Logger.info("Null gps location for target: " + target.toString())
    }
    odlcProto.setAutonomous(target.creator.equals(ClientType.ADLC))
    odlcProto.setMission(MISSION_ID.toInt())

    if (target.typeString.equals("Alphanum")) {
      val castedTarget = target as AlphanumTarget
      val targetType = if (target.isOffaxis) Odlc.Type.OFF_AXIS else Odlc.Type.STANDARD
      if (null != castedTarget.alpha) odlcProto.setAlphanumeric(castedTarget.alpha)
      if (null != castedTarget.shape) odlcProto.setShape(castedTarget.shape.asProtoShape())
      if (null != castedTarget.alphaColor) odlcProto.setAlphanumericColor(castedTarget.alphaColor.asProtoColor())
      if (null != castedTarget.shapeColor) odlcProto.setShapeColor(castedTarget.shapeColor.asProtoColor())
      if (null != castedTarget.geotag?.radiansFromNorth) odlcProto.setOrientation(CardinalDirection.getFromRadians(castedTarget.geotag.radiansFromNorth).asProtoOrientation())
      odlcProto.setType(targetType)
    } else {
      val castedTarget = target as EmergentTarget
      if (null != castedTarget.description) odlcProto.setDescription(castedTarget.description)
        .setType(Odlc.Type.EMERGENT)
    }
    return odlcProto.build()
  }

  public void 


  public void attemptLogin() {

    System.out.println("ATTEMPTING LOGIN");

    String loginURL = "http://" + INTEROP_IP + ":" + INTEROP_PORT + LOGIN;

    URI interopURI = URI.create(loginURL);

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    Credentials credentials = Credentials.newBuilder()
      .setUsername(USERNAME)
      .setPassword(PASSWORD)
      .build();

    // todo with protobuf 
    JSONObject personJsonObject = new JSONObject();
    personJsonObject.put("username", USERNAME);
    personJsonObject.put("password", PASSWORD);

    try {
      HttpEntity<String> requestEntity = new HttpEntity<String>(JsonFormat.printer().print(credentials), headers);
      ListenableFuture<ResponseEntity<String>> loginFuture = template.exchange(
        interopURI, HttpMethod.POST, requestEntity, String.class);
      // todo not just 0 // todo error handling
      RequestUtil.SuccessCallback<String> loginCallback = (ResponseEntity<String> result)-> {
        String cookieString = result.getHeaders().get(HttpHeaders.SET_COOKIE).get(0);
        cookieValue = HttpCookie.parse(cookieString).get(0).getValue();   //(HttpCookie.parse(result.getHeaders().get(HttpHeaders.SET_COOKIE).get(0)).get(0).getValue());
        getMissionInfo();
      };
    System.out.println(cookieValue);
      RequestUtil.futureCallback(loginURL, loginFuture, loginCallback);
    } catch (Exception e) {
      logger.error("Interop Client login: " + e.getMessage());

    }
    
  }

}

