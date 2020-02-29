package org.cuair.ground.clients;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.AlphanumTargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;
import org.cuair.ground.models.geotag.*;
import org.cuair.ground.models.plane.target.*;
import org.cuair.ground.protobuf.InteropApi.*;
import org.cuair.ground.util.*;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

/*
 * Client for interop communications
 */
public class InteropClient {

  private static final Logger logger = LoggerFactory.getLogger(InteropClient.class);

  private ClientCreatableDatabaseAccessor<EmergentTarget> emergentTargetDao =
      (ClientCreatableDatabaseAccessor<EmergentTarget>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget.class);

  private AlphanumTargetDatabaseAccessor<AlphanumTarget> alphaTargetDao =
      (AlphanumTargetDatabaseAccessor<AlphanumTarget>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

  private static final AlphanumTargetSightingsDatabaseAccessor<AlphanumTargetSighting>
      alphaTargetSightingDao =
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

  public String TARGET_ROUTE = Flags.TARGET_ROUTE;

  public int IMAGE_WIDTH = Flags.IMAGE_WIDTH;

  public int IMAGE_HEIGHT = Flags.IMAGE_HEIGHT;

  public String cookieValue;

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
    double latitude = emergentPos.getLatitude();
    double longitude = emergentPos.getLongitude();
    double altitude = emergentPos.getAltitude();

    GpsLocation gpsLocation = null;
    try {
      gpsLocation = new GpsLocation(latitude, longitude);
    } catch (InvalidGpsLocationException e) {
      logger.error("Invalid GPS location for emergent.");
    }

    Geotag geotag = new Geotag(gpsLocation, null);
    emergentTarget.setGeotag(geotag);
    emergentTargetDao.update(emergentTarget);
    attemptSend(emergentTarget);
  }

  public void processAirdrop(Mission missionInfo) {}

  public void getMissionInfo() {
    URI missionURI = URI.create("http://" + INTEROP_IP + ":" + INTEROP_PORT + MISSION_INFO);
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(cookieValue);
    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
    ListenableFuture<ResponseEntity<String>> missionFuture =
        template.exchange(missionURI, HttpMethod.GET, requestEntity, String.class);

    // todo printing error messages when success callback is bad
    RequestUtil.SuccessCallback<String> missionCallback =
        (ResponseEntity<String> result) -> {
          System.out.println("GOT MISSION SUCCESS");
          Mission.Builder missionBuilder = Mission.newBuilder();
          try {
            // int idd = result.getBody().getId();
            JsonFormat.parser()
                .merge(result.getBody().substring(1, result.getBody().length()), missionBuilder);
            Mission missionInfo = missionBuilder.build();
            processOffAxisPos(missionInfo);
            processEmergent(missionInfo);

          } catch (Exception e) // todo specific exception
          {
            logger.error("Error parsing mission information: " + e.getMessage());
          }

          // init emergent
          // init off axis
          // init airdrop
        };
    RequestUtil.futureCallback(missionURI, missionFuture, missionCallback);
  }

  public EmergentTarget getEmergentTarget() {
    List<EmergentTarget> emergentTargets = emergentTargetDao.getAll();
    EmergentTarget emergentTarget = emergentTargets.isEmpty() ? null : emergentTargets.get(0);
    return emergentTarget;
  }

  public void attemptUpdateImage(Target target) {
    URI updateImageURI =
        URI.create(
            "http://"
                + INTEROP_IP
                + ":"
                + INTEROP_PORT
                + TARGET_ROUTE
                + "/"
                + target.getJudgeTargetId()
                + "/image");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", String.format("sessionid=%s", cookieValue));
    headers.setContentType(MediaType.IMAGE_JPEG);
    try {
      HttpEntity<byte[]> requestEntity =
          new HttpEntity<byte[]>(IOUtils.toByteArray(getIS(target)), headers);
      ListenableFuture<ResponseEntity<String>> updateImageFuture =
          template.exchange(updateImageURI, HttpMethod.POST, requestEntity, String.class);
      RequestUtil.futureCallback(updateImageURI, updateImageFuture);
    } catch (IOException e) {
      logger.error("Error when updating image: " + e.getMessage());
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
      // Try for approx. 90 sec.
      int i = 0;
      while (this.target.getJudgeTargetId() == null && i++ != 2) {
        if (i == 1) {
          logger.error("Pending update target thumbnail until JudgeTargetId");
        }
        try {
          Thread.sleep(Flags.TARGETLOGGER_DELAY); // todo
        } catch (InterruptedException e) {
        }
        // todo emergent vs alpha

        this.target = alphaTargetDao.get(this.targetSighting.getTarget().getId());
      }
      if (this.target.getJudgeTargetId() == null) {
        logger.error(
            "Can't update target thumbnail: Target does not exist on the competition server");
      }
      if (this.targetSighting.getAssignment() == null
          || this.targetSighting.getAssignment().getImage() == null) {
        logger.error("Can't update target thumbnail: TargetSighting doesn't have image");
      }
      saveTargetThumbnailFile(this.targetSighting);
      attemptUpdateImage(this.target);
    }
  }

  /**
   * Updates a target's thumbnail image on the competition server
   *
   * <p>Precondition: target must be an entry in our database
   *
   * @param targetSighting Sighting of target whose image will represent its target
   * @throws IllegalArgumentException if precondition isn't satisfied precondition: target must not
   *     be null and must have a judge target id, and must have an associated image
   */
  public void updateTargetImage(TargetSighting targetSighting) {
    Target target;
    if (targetSighting instanceof EmergentTargetSighting) {
      target = emergentTargetDao.get(targetSighting.getTarget().getId());
    } else {
      target = alphaTargetDao.get(targetSighting.getTarget().getId());
    }
    if (target == null) {
      logger.info("Can't update target thumbnail: Target object is null");
      return;
    }
    UpdateTarget updateTarget = new UpdateTarget(target, targetSighting);
    Thread t = new Thread(updateTarget);
    t.start();
  }

  /**
   * Saves a thumbnail of a target sighting for a target
   *
   * @param targetSighting TargetSighting of the Target
   */
  private BufferedImage saveTargetThumbnailFile(TargetSighting targetSighting) {
    String imageURL =
        "images/" + targetSighting.getAssignment().getImage().getImageUrl().substring(19);
    File imgFile = new File(imageURL);
    BufferedImage in = null;

    try {
      in = ImageIO.read(imgFile);
    } catch (IOException e) {
      logger.error("Cannot read image " + imageURL + " from image directory " + e.getMessage());
    }

    int topLeftX = Math.max(0, targetSighting.getpixel_x() - targetSighting.getWidth() / 2);
    int topLeftY = Math.max(0, targetSighting.getpixel_y() - targetSighting.getHeight() / 2);
    int width = Math.min((IMAGE_WIDTH - topLeftX), targetSighting.getWidth());
    int height = Math.min((IMAGE_HEIGHT - topLeftY), targetSighting.getHeight());
    BufferedImage newImage = in.getSubimage(topLeftX, topLeftY, width, height);

    if (targetSighting instanceof EmergentTargetSighting) {
      try {
        EmergentTarget target = emergentTargetDao.get(targetSighting.getTarget().getId());
        File outputfile = getTargetInteropImage(target);
        ImageIO.write(newImage, "png", outputfile);
      } catch (IOException e) {
        logger.error("Cannot create thumbnail of target sighting " + e.getMessage());
      }
      logger.info("Target Image Export Successful");
      return newImage;
    } else {
      try {
        AlphanumTarget target = alphaTargetDao.get(targetSighting.getTarget().getId());
        File outputfile = getTargetInteropImage(target);
        ImageIO.write(newImage, "png", outputfile);
      } catch (IOException e) {
        logger.error("Cannot create thumbnail of target sighting " + e.getMessage());
      }

      logger.info("Target Image Export Successful");
      return newImage;
    }
  }

  public File getTargetInteropImage(Target t) {
    // todo wait if judge target id is not there yet?
    File file = new File(targetDirectory + t.getJudgeTargetId() + ".png");
    try {
      file.createNewFile();
    } catch (IOException e) {
    }
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
    File file = new File(targetDirectory + t.getJudgeTargetId() + ".png"); // todo
    InputStream targetStream = null; // todo
    try {
      targetStream = new FileInputStream(file);
    } catch (Exception e) {
      logger.error("Error when getting file stream: " + e.getMessage());
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
      AlphanumTarget alphanumTarget = ((AlphanumTarget) target);
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
      if (alphanumTarget.getGeotag() != null
          && alphanumTarget.getGeotag().getRadiansFromNorth() != null) {
        odlcProto.setOrientation(
            CardinalDirection.getFromRadians(alphanumTarget.getGeotag().getRadiansFromNorth())
                .asProtoOrientation());
      }
      odlcProto.setType(Odlc.Type.STANDARD);
    } else {
      EmergentTarget emergentTarget = (EmergentTarget) target;
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
    } catch (InvalidProtocolBufferException e) {

    }
    return odlcBuilder.build();
  }

  public void attemptSend(Target target) {
    Odlc odlcProto = createOdlcProto(target);
    URI postODLCURI = URI.create("http://" + INTEROP_IP + ":" + INTEROP_PORT + TARGET_ROUTE);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", String.format("sessionid=%s", cookieValue));
    System.out.println(cookieValue);
    try {
      HttpEntity<String> requestEntity =
          new HttpEntity<String>(JsonFormat.printer().print(odlcProto), headers);
      ListenableFuture<ResponseEntity<String>> postTargetFuture =
          template.exchange(postODLCURI, HttpMethod.POST, requestEntity, String.class);

      // todo printing error messages when success callback is bad
      RequestUtil.SuccessCallback<String> postOdlcCallback =
          (ResponseEntity<String> result) -> {
            Odlc odlcInfo = getOdlcDataAsProto(result.getBody());
            target.setJudgeTargetId(Long.valueOf(odlcInfo.getId()));
            if (target instanceof AlphanumTarget) {
              alphaTargetDao.update((AlphanumTarget) target);
            } else if (target instanceof EmergentTarget) {
              emergentTargetDao.update((EmergentTarget) target);
            }

            // Mission.Builder missionBuilder = Mission.newBuilder();
            // //System.out.println(Mission.newBuilder().getClass());

            // //System.out.println(result.getBody());
            // try {
            //   //int idd = result.getBody().getId();
            //   //System.out.println(idd + " fs");
            //   JsonFormat.parser().merge(result.getBody().substring(1, result.getBody().length()),
            // missionBuilder);
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

      RequestUtil.futureCallback(postODLCURI, postTargetFuture, postOdlcCallback);
    } catch (InvalidProtocolBufferException e) {
      logger.error("Error when sending target: " + e.getMessage());
    }
  }

  public void attemptDelete(Target t) {
    // String deleteURL = // todo rename route
    System.out.println(cookieValue);
    // todo combine all interop stuff
    URI deleteURI =
        URI.create(
            "http://"
                + INTEROP_IP
                + ":"
                + INTEROP_PORT
                + TARGET_ROUTE
                + "/"
                + t.getJudgeTargetId());
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(cookieValue);
    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
    ListenableFuture<ResponseEntity<String>> deleteFuture =
        template.exchange(deleteURI, HttpMethod.DELETE, requestEntity, String.class);

    RequestUtil.futureCallback(deleteURI, deleteFuture);
  }

  public void attemptLogin() {
    URI interopURI = URI.create("http://" + INTEROP_IP + ":" + INTEROP_PORT + LOGIN);

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    Credentials credentials =
        Credentials.newBuilder().setUsername(USERNAME).setPassword(PASSWORD).build();

    // todo with protobuf
    JSONObject personJsonObject = new JSONObject();
    personJsonObject.put("username", USERNAME);
    personJsonObject.put("password", PASSWORD);

    try {
      HttpEntity<String> requestEntity =
          new HttpEntity<String>(JsonFormat.printer().print(credentials), headers);
      ListenableFuture<ResponseEntity<String>> loginFuture =
          template.exchange(interopURI, HttpMethod.POST, requestEntity, String.class);
      RequestUtil.SuccessCallback<String> loginCallback =
          (ResponseEntity<String> result) -> {
            System.out.println("SUCCESS LOGIN");
            try {
              String cookieString = result.getHeaders().get(HttpHeaders.SET_COOKIE).get(0);
              cookieValue =
                  HttpCookie.parse(cookieString)
                      .get(0)
                      .getValue(); // (HttpCookie.parse(result.getHeaders().get(HttpHeaders.SET_COOKIE).get(0)).get(0).getValue());
            } catch (Exception e) {
              logger.error("Error when parsing cookie: " + e.getMessage());
            }
            System.out.println(cookieValue);
            getMissionInfo();
          };
      RequestUtil.futureCallback(interopURI, loginFuture, loginCallback);
    } catch (Exception e) {
      logger.error("Interop Client login: " + e.getMessage());
    }
  }
}
