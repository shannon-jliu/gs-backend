package org.cuair.ground.clients;

import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import java.awt.image.BufferedImage;

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
import java.io.File;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import java.net.HttpCookie;
import javax.imageio.ImageIO;
import java.util.List;

import org.cuair.ground.models.plane.target.*;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.geotag.*;
import org.cuair.ground.models.plane.target.*;
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.AlphanumTargetSightingsDatabaseAccessor;
import java.lang.Runnable;
import org.apache.commons.io.IOUtils;
import java.net.URL;
import java.io.InputStream;
import java.io.FileInputStream;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;

/*
 * Client for interop communications
 */
public class InteropClient {

  private static final Logger logger = LoggerFactory.getLogger(InteropClient.class);

  private ClientCreatableDatabaseAccessor<EmergentTarget> emergentTargetDao = (ClientCreatableDatabaseAccessor<EmergentTarget>)DAOFactory.getDAO(
        DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget.class);

  private AlphanumTargetDatabaseAccessor<AlphanumTarget> alphaTargetDao = (AlphanumTargetDatabaseAccessor<AlphanumTarget>)DAOFactory.getDAO(
        DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

  private static final AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting> alphaTargetSightingDao =
        (AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting>)
            DAOFactory.getDAO(
                DAOFactory.ModelDAOType.ALPHANUM_TARGET_SIGHTINGS_DATABASE_ACCESSOR,
                AlphanumTargetSighting.class);

  private AsyncRestTemplate template = new AsyncRestTemplate();

  public String INTEROP_IP = "localhost";

  public String INTEROP_PORT = "8000";

  public String LOGIN = "/api/login";

  public String USERNAME = "testadmin";

  public String PASSWORD = "testpass";

  public String MISSION_INFO = Flags.MISSION_INFO;

  public int MISSION_ID = Flags.MISSION_ID;

  public String POST_TARGET = Flags.POST_TARGET;

  public int IMAGE_WIDTH = Flags.IMAGE_WIDTH;

  public int IMAGE_HEIGHT = Flags.IMAGE_HEIGHT;

  //public String UPDATE_IMAGE = Flags.UPDATE_IMAGE;

  public String cookieValue = ""; // TODO

  public String targetDirectory = Flags.INTEROP_TARGET_DIR;

  public void processOffAxisPos(Mission missionInfo) {
    AlphanumTarget offAxisTarget = alphaTargetDao.getOffaxisTarget();
    if (offAxisTarget.getJudgeTargetId() != null) return;

    Position offAxisPos = missionInfo.getOffAxisOdlcPos();
    double latitude = offAxisPos.getLatitude();
    double longitude = offAxisPos.getLongitude();
    double altitude = offAxisPos.getAltitude();

    GpsLocation gpsLocation = null;
    try {
      gpsLocation = new GpsLocation(latitude, longitude);
    } catch (InvalidGpsLocationException e) {
      logger.error("Invalid GPS location for off axis.");
    }

    Geotag geotag = new Geotag(gpsLocation, null);
    
    offAxisTarget.setGeotag(geotag);
    alphaTargetDao.update(offAxisTarget);
    attemptSend(offAxisTarget);
    
  }

  public void processEmergent(Mission missionInfo) {
    EmergentTarget emergentTarget = getEmergentTarget();
    if (emergentTarget.getJudgeTargetId() != null) return;

    Position emergentPos = missionInfo.getEmergentLastKnownPos();
    System.out.println("emergent " + emergentPos);
    double latitude = emergentPos.getLatitude();
    double longitude = emergentPos.getLongitude();
    System.out.println("emergent " + latitude);
    System.out.println("emergent " + longitude);
    double altitude = emergentPos.getAltitude();

    GpsLocation gpsLocation = null;
    try {
      gpsLocation = new GpsLocation(latitude, longitude);
    } catch (InvalidGpsLocationException e) {
      logger.error("Invalid GPS location for off axis.");
    }

    Geotag geotag = new Geotag(gpsLocation, null);
    emergentTarget.setGeotag(geotag);
    emergentTargetDao.update(emergentTarget);
    attemptSend(emergentTarget);
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
        processOffAxisPos(missionInfo);
        processEmergent(missionInfo);
        
      } catch (Exception e) { // todo specific exception
        System.out.println("ERROR " + e.getMessage());
      }

      // init emergent
      // init off axis
      // init airdrop
    };

    RequestUtil.futureCallback(missionURL, missionFuture, missionCallback);
  }

  public EmergentTarget getEmergentTarget() {
    List<EmergentTarget> emergentTargets = emergentTargetDao.getAll();
    EmergentTarget emergentTarget = emergentTargets.isEmpty() ? null : emergentTargets.get(0);
    return emergentTarget;
  }

  public void attemptUpdateImage(Target target) {
    String updateImageURL = "http://" + INTEROP_IP + ":" + INTEROP_PORT + POST_TARGET + "/" + target.getJudgeTargetId() + "/image";
    System.out.println(updateImageURL);
    URI updateImageURI = URI.create(updateImageURL);
    HttpHeaders headers = new HttpHeaders();
    System.out.println(cookieValue);
    headers.add("Cookie", String.format("sessionid=%s", cookieValue));
    headers.setContentType(MediaType.IMAGE_JPEG);
    try {
      HttpEntity<byte[]> requestEntity = new HttpEntity<byte[]>(IOUtils.toByteArray(getIS(target)), headers);
      ListenableFuture<ResponseEntity<String>> updateImageFuture = template.exchange(
          updateImageURI, HttpMethod.POST, requestEntity, String.class);

      // todo printing error messages when success callback is bad
      // RequestUtil.SuccessCallback<String> updateImageCallback = (ResponseEntity<String> result)-> {
      //   System.out.println("HERE SUCCESS");
      // };

      RequestUtil.futureCallback(updateImageURL, updateImageFuture);
    } catch (Exception e) {
      System.out.println("EXCEPTION  here HERE: " + e.getMessage());
    }
  }


  class UpdateTarget implements Runnable {

      Target target;
      TargetSighting targetSighting;

      public UpdateTarget(Target target, TargetSighting targetSighting) {
        this.target = target;
        this.targetSighting = targetSighting;
      }

      public void run() {
          System.out.println("Hello from a thread!");
          // Try for approx. 90 sec
          int i = 0;
          while (this.target.getJudgeTargetId() == null && i++ != 2) {
            if (i == 1) {
              logger.error("Pending update target thumbnail until JudgeTargetId");
            }
            try {
              Thread.sleep(Flags.TARGETLOGGER_DELAY); // todo
            } catch (InterruptedException e) {}
            // todo emergent vs alpha

            this.target = alphaTargetDao.get(this.targetSighting.getTarget().getId());
          }
          if (this.target.getJudgeTargetId() == null) {
            logger.error("Can't update target thumbnail: Target does not exist on the competition server");
          }
          if (this.targetSighting.getAssignment() == null || this.targetSighting.getAssignment().getImage() == null) {
            logger.error("Can't update target thumbnail: TargetSighting doesn't have image");
          }
          saveTargetThumbnailFile(this.targetSighting);
          attemptUpdateImage(this.target);
      }
  }

  /**
   * Updates a target's thumbnail image on the competition server
   *
   * Precondition: target must be an entry in our database
   *
   * @param targetSighting Sighting of target whose image will represent its target
   * @throws IllegalArgumentException if precondition isn't satisfied precondition: target must not
   * be null and must have a judge target id, and must have an associated image
   */
  public void updateTargetImage(TargetSighting targetSighting) {
    //enabledVoid {
    System.out.println("update target image");

    if (targetSighting instanceof EmergentTargetSighting) {
      EmergentTarget target = emergentTargetDao.get(targetSighting.getTarget().getId());
      if (target == null) {
        logger.info("Can't update target thumbnail: Target object is null");
      }

      UpdateTarget updateTarget = new UpdateTarget(target, targetSighting);

      Thread t = new Thread(updateTarget);
      t.start();

      // thread {
      //   // Try for approx. 90 sec
      //   while (target?.judgeTargetId == null && i++ != 2) {
      //     if (i == 1) Logger.error("Pending update target thumbnail until JudgeTargetId")
      //     Thread.sleep(PlayConfig.TARGETLOGGER_DELAY)
      //     target = emergentTargetDao.get(targetSighting.getTarget().getId())
      //   }

      //   requireNotNull(target?.judgeTargetId) { "Can't update target thumbnail: Target does not exist on the competition server" }
      //   requireNotNull(targetSighting.assignment?.image) { "Can't update target thumbnail: TargetSighting doesn't have image" }
      //   this.saveTargetThumbnailFile(targetSighting)
      //   queue.add(PostImageRunnable(target))
      // }
      
    } else {
      logger.info("interop client alphanum");
      int i = 0;
      AlphanumTarget target = alphaTargetDao.get(targetSighting.getTarget().getId());
      if (target == null) {
        logger.info("Can't update target thumbnail: Target object is null");
        return;
      }
      UpdateTarget updateTarget = new UpdateTarget(target, targetSighting);

      Thread t = new Thread(updateTarget);
      t.start();

      // requireNotNull(target) { "Can't update target thumbnail: Target object is null" }
      // thread {
      //   // Try for approx. 90 sec
      //   while (target?.judgeTargetId == null && i++ != 2) {
      //     Logger.info("WAITING");
      //     if (i == 1) Logger.error("Pending update target thumbnail until JudgeTargetId")
      //     Thread.sleep(PlayConfig.TARGETLOGGER_DELAY)
      //     target = alphaTargetDao.get(targetSighting.getTarget().getId())
      //   }
      //   Logger.info("here");

      //   requireNotNull(target?.judgeTargetId) { "Can't update target thumbnail: Target does not exist on the competition server" }
      //   requireNotNull(targetSighting.assignment?.image) { "Can't update target thumbnail: TargetSighting doesn't have image" }
      //   this.saveTargetThumbnailFile(targetSighting)
      //   queue.add(PostImageRunnable(target))
      // }
    }
      //targetSighting.typeString.equals("Alphanum") ? alphaTargetDao : emergentTargetDao
    //}
  }



  /**
   * Saves a thumbnail of a target sighting for a target
   *
   * @param targetSighting TargetSighting of the Target
   */
  private BufferedImage saveTargetThumbnailFile(TargetSighting targetSighting) {
    System.out.println(targetSighting.getAssignment());
    System.out.println(targetSighting.getAssignment().getImage());
    System.out.println(targetSighting.getAssignment().getImage().getImageUrl());
    String actual = "images/" + targetSighting.getAssignment().getImage().getImageUrl().substring(19);
    File imgFile = new File(actual);
    BufferedImage in = null;

    try {
      in = ImageIO.read(imgFile);
    } catch (IOException e) {
      logger.error("Cannot read image filjufrhfrfve from image directory ${e.message}");
    }

    int topLeftX = Math.max(0, targetSighting.getpixel_x() - targetSighting.getWidth() / 2);
    int topLeftY = Math.max(0, targetSighting.getpixel_y() - targetSighting.getHeight() / 2);
    int width = Math.min((IMAGE_WIDTH - topLeftX), targetSighting.getWidth());
    int height = Math.min((IMAGE_HEIGHT - topLeftY), targetSighting.getHeight());
    System.out.println(topLeftX + " and toplefty " + topLeftY + " a " + width + " and h " + height);
    BufferedImage newImage = in.getSubimage(topLeftX, topLeftY, width, height);

    if (targetSighting instanceof EmergentTargetSighting) {
      try {
        EmergentTarget target = emergentTargetDao.get(targetSighting.getTarget().getId());
        File outputfile = getTargetInteropImage(target);
        ImageIO.write(newImage, "png", outputfile);
      } catch (IOException e) {
        logger.error("Cannot create thumbnail of target sighting ${e.message}");
      }
      logger.info("Target Image Export Successful");
      return newImage;
    } else {
      try {
        AlphanumTarget target = alphaTargetDao.get(targetSighting.getTarget().getId());
        File outputfile = getTargetInteropImage(target);
        ImageIO.write(newImage, "png", outputfile);
      } catch (IOException e) {
        logger.error("Cannot create thumbnail of target sighting ${e.message}");
      }

      logger.info("Target Image Export Successful");
      return newImage;    
    }
  }

  public File getTargetInteropImage(Target t) {
    //System.out.println("target interop image " + t.getJudgeTargetId());
    // todo wait if judge target id is not there yet?
    File file = new File(targetDirectory + t.getJudgeTargetId() + ".png");
    try {
      file.createNewFile();
    } catch (Exception e) {}
    // creates target directory if it doesn't exist already
    //file.getParentFile().mkdirs();
    return file;
  }

  public InputStream getIS(Target t) {
    // InputStream input = null;
    // try {
    //   input = new URL(targetDirectory + t.getJudgeTargetId() + ".jpg").openStream();
    // } catch (Exception e) { // todo
    //   System.out.println("HERE error 5 " + e.getMessage());
    //   return null;
    // }
    AlphanumTargetSighting alphaTargetSighting = alphaTargetSightingDao.get(t.getthumbnail_tsid());
    String imageUrl = alphaTargetSighting.getAssignment().getImage().getImageUrl();
    String actual = "images/" + imageUrl.substring(19);
    System.out.println("actual url " + actual);

    File file = new File(targetDirectory + t.getJudgeTargetId() + ".png"); // todo
    //File file = new File(actual); // todo
    // creates target directory if it doesn't exist already
    file.getParentFile().mkdirs();
    InputStream targetStream = null; //todo
    try {
      targetStream = new FileInputStream(file);
    } catch (Exception e) {
      System.out.println("getis error " + e.getMessage());
    }
    return targetStream;
  }

  public Odlc createOdlcProto(Target target) {
    Odlc.Builder odlcProto = Odlc.newBuilder();
    if (target.getGeotag() != null && target.getGeotag().getGpsLocation() != null) {
      odlcProto
        .setLatitude(target.getGeotag().getGpsLocation().getLatitude())
        .setLongitude(target.getGeotag().getGpsLocation().getLongitude());
    } else {
      logger.error("Null gps location for target: " + target.toString());
    }
    odlcProto.setAutonomous(target.getCreator().equals(ClientType.ADLC));
    odlcProto.setMission(MISSION_ID);
    if (target.getTypeString().equals("Alphanum")) {
      AlphanumTarget alphanumTarget = ((AlphanumTarget)target);
      if (alphanumTarget.getAlpha() != null) {
        odlcProto.setAlphanumeric(alphanumTarget.getAlpha());
      }
      if (alphanumTarget.getShape() != null) {
        odlcProto.setShape(alphanumTarget.getShape().asProtoShape());
      }
      if (alphanumTarget.getAlphaColor() != null) {
        odlcProto.setAlphanumericColor(alphanumTarget.getAlphaColor().asProtoColor());
      }
      if (alphanumTarget.getShapeColor() != null) {
        odlcProto.setShapeColor(alphanumTarget.getShapeColor().asProtoColor());
      }
      if (alphanumTarget.getGeotag() != null && alphanumTarget.getGeotag().getRadiansFromNorth() != null) {
        odlcProto.setOrientation(CardinalDirection.getFromRadians(alphanumTarget.getGeotag().getRadiansFromNorth()).asProtoOrientation());
      } 
      odlcProto.setType(Odlc.Type.STANDARD);
    } else {
      EmergentTarget emergentTarget = (EmergentTarget)target;
      if (emergentTarget.getDescription() != null) {
        odlcProto.setDescription(emergentTarget.getDescription());
        odlcProto.setType(Odlc.Type.EMERGENT);
      }
    }
    return odlcProto.build();
  }

  public Odlc getOdlcDataAsProto(String odlcInfo) {
    Odlc.Builder odlcBuilder = Odlc.newBuilder();
    try {
      JsonFormat.parser().merge(odlcInfo, odlcBuilder);
    } catch (Exception e) { // todo InvalidProtocolBufferException

    }
    return odlcBuilder.build();
  }

  public void attemptSend(Target target) {
    System.out.println("attemptSend");

    Odlc odlcProto = createOdlcProto(target);

    String postODLCURL = "http://" + INTEROP_IP + ":" + INTEROP_PORT + POST_TARGET;
    System.out.println(postODLCURL);
    URI postODLCURI = URI.create(postODLCURL);
    HttpHeaders headers = new HttpHeaders();
    System.out.println(cookieValue);
    headers.add("Cookie", String.format("sessionid=%s", cookieValue));
    try {
      HttpEntity<String> requestEntity = new HttpEntity<String>(JsonFormat.printer().print(odlcProto), headers);
      ListenableFuture<ResponseEntity<String>> postTargetFuture = template.exchange(
          postODLCURI, HttpMethod.POST, requestEntity, String.class);

      // todo printing error messages when success callback is bad
      RequestUtil.SuccessCallback<String> postOdlcCallback = (ResponseEntity<String> result)-> {
        Odlc odlcInfo = getOdlcDataAsProto(result.getBody());

        System.out.println("HERE SUCCESS attemptsend " + odlcInfo.getId());
        target.setJudgeTargetId(Long.valueOf(odlcInfo.getId()));
        if (target instanceof AlphanumTarget) {
          alphaTargetDao.update((AlphanumTarget)target);
        } else if (target instanceof EmergentTarget) {
          emergentTargetDao.update((EmergentTarget)target);
        }

        // Mission.Builder missionBuilder = Mission.newBuilder();
        // //System.out.println(Mission.newBuilder().getClass());
        
        // //System.out.println(result.getBody());
        // try {
        //   //int idd = result.getBody().getId();
        //   //System.out.println(idd + " fs");
        //   JsonFormat.parser().merge(result.getBody().substring(1, result.getBody().length()), missionBuilder);
        //   System.out.println("heree");
        //   Mission missionInfo = missionBuilder.build();
        //   System.out.println(missionInfo.getOffAxisOdlcPos().getLatitude());
        // } catch (Exception e) { // todo specific exception
        //   System.out.println("ERROR " + e.getMessage());
        // }

        // init emergent
        // init off axis
        // init airdrop
      };

      RequestUtil.futureCallback(postODLCURL, postTargetFuture, postOdlcCallback);
    } catch (Exception e) {
      System.out.println("EXCEPTION HERE: " + e.getMessage());
    }
  }

  public void attemptDelete(Target t) {
    String deleteURL = "http://" + INTEROP_IP + ":" + INTEROP_PORT + POST_TARGET + "/" + t.getJudgeTargetId(); // todo rename route
    System.out.println(cookieValue);
    URI deleteURI = URI.create(deleteURL);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", String.format("sessionid=%s", cookieValue));
    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
    ListenableFuture<ResponseEntity<String>> deleteFuture = template.exchange(
        deleteURI, HttpMethod.DELETE, requestEntity, String.class);

    // todo printing error messages when success callback is bad
    RequestUtil.SuccessCallback<String> deleteCallback = (ResponseEntity<String> result)-> {
      System.out.println("HERE AFTER DELETE");
      
      // init emergent
      // init off axis
      // init airdrop
    };

    RequestUtil.futureCallback(deleteURL, deleteFuture, deleteCallback);
  }

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

