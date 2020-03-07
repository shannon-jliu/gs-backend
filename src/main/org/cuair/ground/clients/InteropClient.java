package org.cuair.ground.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.AlphanumTargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.PlaneSettingsModelDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;
import org.cuair.ground.models.geotag.*;
import org.cuair.ground.models.plane.settings.*;
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

  private static AirdropClient airdropClient = ClientFactory.getAirdropClient();

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

  private static final PlaneSettingsModelDatabaseAccessor<AirdropSettings> airdropDao = 
      (PlaneSettingsModelDatabaseAccessor<AirdropSettings>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.PLANE_SETTINGS_MODEL_DATABASE_ACCESSOR, 
              AirdropSettings.class);


  private AsyncRestTemplate template = new AsyncRestTemplate();

  public String INTEROP_IP = "localhost";

  public String INTEROP_PORT = "8000";

  public String INTEROP_ADDRESS = "http://" + INTEROP_IP + ":" + INTEROP_PORT;

  public String LOGIN = "/api/login";

  public String USERNAME = "testadmin";

  public String PASSWORD = "testpass";

  public String TARGET_ROUTE = Flags.TARGET_ROUTE;

  public String TARGET_DIRECTORY = Flags.INTEROP_TARGET_DIR;

  public String cookieValue;

  public GpsLocation getGpsLoc(Position pos) {
    double latitude = pos.getLatitude();
    double longitude = pos.getLongitude();
    double altitude = pos.getAltitude();

    GpsLocation gpsLocation = null;
    try {
      gpsLocation = new GpsLocation(latitude, longitude);
    } catch (InvalidGpsLocationException e) {
      logger.error("Invalid GPS location for off axis.");
    }
    return gpsLocation;
  }

  public void processOffAxisPos(Mission missionInfo) {
    AlphanumTarget offAxisTarget = alphaTargetDao.getOffaxisTarget();
    if (offAxisTarget.getJudgeTargetId() != null) return;

    Position offAxisPos = missionInfo.getOffAxisOdlcPos();
    GpsLocation offAxisLocation = getGpsLoc(offAxisPos);
    Geotag geotag = new Geotag(offAxisLocation, null);
    offAxisTarget.setGeotag(geotag);
    alphaTargetDao.update(offAxisTarget);
    attemptSend(offAxisTarget);
  }

  public void processEmergent(Mission missionInfo) {
    EmergentTarget emergentTarget = getEmergentTarget();
    if (emergentTarget.getJudgeTargetId() != null) return;
    Position emergentPos = missionInfo.getEmergentLastKnownPos();
    GpsLocation emergentLocation = getGpsLoc(emergentPos);
    Geotag geotag = new Geotag(emergentLocation, null);
    emergentTarget.setGeotag(geotag);
    emergentTargetDao.update(emergentTarget);
    attemptSend(emergentTarget);
  }

  public void processAirdrop(Mission missionInfo) {
    Position airdropPos = missionInfo.getAirDropPos();
    GpsLocation airdropLocation = getGpsLoc(airdropPos);
    AirdropSettings as = new AirdropSettings(airdropLocation, Flags.CUAIR_AIRDROP_THRESHOLD - 1, false, false);
    airdropClient.changeMode(as);
    airdropDao.create(as);
  }

  public void getMissionInfo() {
    URI missionURI = URI.create(INTEROP_ADDRESS + Flags.MISSION_INFO);
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(cookieValue);
    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
    ListenableFuture<ResponseEntity<String>> missionFuture =
        template.exchange(missionURI, HttpMethod.GET, requestEntity, String.class);

    RequestUtil.SuccessCallback<String> missionCallback =
        (ResponseEntity<String> result) -> {
          Mission.Builder missionBuilder = Mission.newBuilder();
          try {
            JsonFormat.parser()
                .merge(result.getBody().substring(1, result.getBody().length()), missionBuilder);
            Mission missionInfo = missionBuilder.build();
            processOffAxisPos(missionInfo);
            processEmergent(missionInfo);
            processAirdrop(missionInfo);
          } catch (InvalidProtocolBufferException e) {
            logger.error("Error parsing mission information: " + e.getMessage());
          }
        };
    RequestUtil.futureCallback(missionURI, missionFuture, missionCallback);
  }

  public EmergentTarget getEmergentTarget() {
    List<EmergentTarget> emergentTargets = emergentTargetDao.getAll();
    EmergentTarget emergentTarget = emergentTargets.isEmpty() ? null : emergentTargets.get(0);
    return emergentTarget;
  }

  public void attemptUpdate(Target target) {
    Odlc odlcProto = createOdlcProto(target);
    waitForJudgeTargetId(target);
    URI putODLCURI = URI.create(INTEROP_ADDRESS + TARGET_ROUTE + "/" + target.getJudgeTargetId());
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(cookieValue);
    try {
      HttpEntity<String> requestEntity =
          new HttpEntity<String>(JsonFormat.printer().print(odlcProto), headers);
      ListenableFuture<ResponseEntity<String>> putTargetFuture =
          template.exchange(putODLCURI, HttpMethod.PUT, requestEntity, String.class);

      RequestUtil.SuccessCallback<String> putOdlcCallback =
          (ResponseEntity<String> result) -> {
            logger.info("Successfully updated " + ((target instanceof AlphanumTarget) ? "Alphanum" : "Emergent") + " target " + target.getId());
            Odlc odlcInfo = getOdlcDataAsProto(result.getBody());
            target.setJudgeTargetId(Long.valueOf(odlcInfo.getId()));
            if (target instanceof AlphanumTarget) {
              alphaTargetDao.update((AlphanumTarget) target);
            } else if (target instanceof EmergentTarget) {
              emergentTargetDao.update((EmergentTarget) target);
            }
          };

      RequestUtil.futureCallback(putODLCURI, putTargetFuture, putOdlcCallback);
    } catch (InvalidProtocolBufferException e) {
      logger.error("Error when updating target: " + e.getMessage());
    }
  }

  public void attemptUpdateImage(Target target) {
    waitForJudgeTargetId(target);
    URI updateImageURI =
        URI.create(INTEROP_ADDRESS + TARGET_ROUTE + "/" + target.getJudgeTargetId() + "/image");
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

  public void waitForJudgeTargetId(Target target) {
    // Try for approx. 90 sec.
    int i = 0;
    while (target.getJudgeTargetId() == null && i++ != 2) {
      if (i == 1) {
        logger.error("Pending update target thumbnail until JudgeTargetId");
      }
      try {
        Thread.sleep(Flags.TARGETLOGGER_DELAY);
      } catch (InterruptedException e) {
      }
      if (target instanceof AlphanumTarget) {
        target = alphaTargetDao.get(target.getId());
      } else {
        target = emergentTargetDao.get(target.getId());
      }
    }
    if (target.getJudgeTargetId() == null) {
      logger.error(
          "Can't update target thumbnail: Target does not exist on the competition server");
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
      waitForJudgeTargetId(this.target);
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
   * Gets the file name where the target's data was stored
   *
   * @param t Target
   * @return String file name
   */
  private File getTargetInteropFile(Target target) {
    waitForJudgeTargetId(target);
    File file = new File(TARGET_DIRECTORY + target.getJudgeTargetId() + ".json");
    return file;
  }

  private void saveTargetFile(Target target) {
    String targetJsonString = null;
    try {
      targetJsonString = (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValueAsString(target.toJson());
    } catch (JsonProcessingException e) {
      logger.error("Unable to pretty print json string: " + e.getMessage());
      targetJsonString = target.toJson().toString();
    }

    File file = getTargetInteropFile(target);
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(file);
      writer.println(targetJsonString);
    } catch (FileNotFoundException e) {
      logger.error("Target Export Failed: " + e.getMessage());
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
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
    int width = Math.min((int)(Flags.IMAGE_WIDTH - topLeftX), targetSighting.getWidth());
    int height = Math.min((int)(Flags.IMAGE_HEIGHT - topLeftY), targetSighting.getHeight());
    BufferedImage newImage = in.getSubimage(topLeftX, topLeftY, width, height);

    Target target;

    if (targetSighting instanceof EmergentTargetSighting) {
      target = emergentTargetDao.get(targetSighting.getTarget().getId());
    } else {
      target = alphaTargetDao.get(targetSighting.getTarget().getId());
    }
    try {
      File outputfile = getTargetInteropImage(target);
      ImageIO.write(newImage, "png", outputfile);
    } catch (IOException e) {
      logger.error("Cannot create thumbnail of target sighting " + e.getMessage());
    }
    logger.info("Target Image Export Successful");
    return newImage;
  }

  public File getTargetInteropImage(Target t) {
    waitForJudgeTargetId(t);
    File file = new File(TARGET_DIRECTORY + t.getJudgeTargetId() + ".png");
    try {
      file.createNewFile();
    } catch (IOException e) {
    }
    return file;
  }

  public InputStream getIS(Target t) {
    waitForJudgeTargetId(t);
    File file = new File(TARGET_DIRECTORY + t.getJudgeTargetId() + ".png");
    InputStream targetStream = null;
    try {
      targetStream = new FileInputStream(file);
    } catch (Exception e) {
      logger.error("Error when getting file stream: " + e.getMessage());
    }
    return targetStream;
  }

  private void removeTargetFile(Target target) {
    Path jsonPath = getTargetInteropFile(target).toPath();
    Path thumbnailPath = getTargetInteropImage(target).toPath();
    try {
      Files.delete(jsonPath);
      if (target.getthumbnail_tsid() != 0L) {
        Files.delete(thumbnailPath);
      }
    } catch (Exception e) {
      logger.error("Unable to delete interop file: " + e.getMessage());
    }
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
    if (target.getJudgeTargetId() != null) {
      odlcProto.setId(target.getJudgeTargetId().intValue());
    }
    odlcProto.setAutonomous(target.getCreator().equals(ClientType.ADLC));
    odlcProto.setMission(Flags.MISSION_ID);
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
      logger.error("Error creating ODLC proto: " + e.getMessage());
    }
    return odlcBuilder.build();
  }

  public void attemptSend(Target target) {
    Odlc odlcProto = createOdlcProto(target);
    URI postODLCURI = URI.create(INTEROP_ADDRESS + TARGET_ROUTE);
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(cookieValue);
    try {
      HttpEntity<String> requestEntity =
          new HttpEntity<String>(JsonFormat.printer().print(odlcProto), headers);
      ListenableFuture<ResponseEntity<String>> postTargetFuture =
          template.exchange(postODLCURI, HttpMethod.POST, requestEntity, String.class);

      RequestUtil.SuccessCallback<String> postOdlcCallback =
          (ResponseEntity<String> result) -> {
            logger.info("Successfully submitted " + ((target instanceof AlphanumTarget) ? "Alphanum" : "Emergent") + " target " + target.getId());
            Odlc odlcInfo = getOdlcDataAsProto(result.getBody());
            target.setJudgeTargetId(Long.valueOf(odlcInfo.getId()));
            if (target instanceof AlphanumTarget) {
              alphaTargetDao.update((AlphanumTarget) target);
            } else if (target instanceof EmergentTarget) {
              emergentTargetDao.update((EmergentTarget) target);
            }
          };

      RequestUtil.futureCallback(postODLCURI, postTargetFuture, postOdlcCallback);
    } catch (InvalidProtocolBufferException e) {
      logger.error("Error when sending target: " + e.getMessage());
    }
  }

  public void attemptDelete(Target t) {
    waitForJudgeTargetId(t);
    URI deleteURI =
        URI.create(INTEROP_ADDRESS + TARGET_ROUTE + "/" + t.getJudgeTargetId());
    HttpHeaders headers = RequestUtil.getDefaultCookieHeaders(cookieValue);
    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
    ListenableFuture<ResponseEntity<String>> deleteFuture =
        template.exchange(deleteURI, HttpMethod.DELETE, requestEntity, String.class);
    RequestUtil.SuccessCallback<String> deleteCallback =
          (ResponseEntity<String> result) -> {
            logger.info("Successfully deleted target " + t.getId());
            removeTargetFile(t);
          };
    RequestUtil.futureCallback(deleteURI, deleteFuture, deleteCallback);
  }

  public void startInteropSequence() {
    URI interopURI = URI.create(INTEROP_ADDRESS + LOGIN);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Credentials credentials =
        Credentials.newBuilder().setUsername(USERNAME).setPassword(PASSWORD).build();
    try {
      HttpEntity<String> requestEntity =
          new HttpEntity<String>(JsonFormat.printer().print(credentials), headers);
      ListenableFuture<ResponseEntity<String>> loginFuture =
          template.exchange(interopURI, HttpMethod.POST, requestEntity, String.class);
      RequestUtil.SuccessCallback<String> loginCallback =
          (ResponseEntity<String> result) -> {
            logger.info("Successfully logged in to Interop.");
            try {
              String cookieString = result.getHeaders().get(HttpHeaders.SET_COOKIE).get(0);
              cookieValue =
                  HttpCookie.parse(cookieString)
                      .get(0)
                      .getValue();
            } catch (Exception e) {
              logger.error("Error when parsing cookie: " + e.getMessage());
            }
            getMissionInfo();
          };
      RequestUtil.futureCallback(interopURI, loginFuture, loginCallback);
    } catch (Exception e) {
      logger.error("Interop Client login: " + e.getMessage());
    }
  }
}
