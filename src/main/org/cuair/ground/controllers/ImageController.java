package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ImageDatabaseAccessor;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.geotag.FOV;
import org.cuair.ground.models.geotag.GimbalOrientation;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.util.Flags;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Contains all the callbacks for all the public api endpoints for the Image */
@CrossOrigin
@RestController
@RequestMapping(value = "/image")
public class ImageController {
  /** Database accessor object for image database */
  private ImageDatabaseAccessor imageDao = (ImageDatabaseAccessor) DAOFactory
      .getDAO(DAOFactory.ModellessDAOType.IMAGE_DATABASE_ACCESSOR);

  /** String path to the folder where all the images are stored */
  private String planeImageDir = Flags.PLANE_IMAGE_DIR;

  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Constructs an HTTP response with all the images after the given id.
   *
   * @param id Long id representing the id after which all images will be returned
   * @return a list of images with ids after the given id on success, 404 when
   * the most recent image in the db does not exist
   */
  @RequestMapping(value = "/all/{id}", method = RequestMethod.GET)
  public ResponseEntity getAllAfterId(@PathVariable Long id) {
    List<Image> images = new ArrayList();
    Image recent = imageDao.getRecent();
    if (recent == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } else {
      Long mostRecentId = recent.getId();
      for (Long index = id + 1; index <= mostRecentId; index++) {
        images.add(imageDao.get(index));
      }
      return ok(images);
    }
  }

  /**
   * Constructs an HTTP response with the most recent image that was captured by the plane.
   *
   * @return 200 with the most recent image that was captured by the plane on success, 404 when
   * the most recent image in the db does not exist
   */
  @RequestMapping(value = "/recent", method = RequestMethod.GET)
  public ResponseEntity getRecent() {
    Image recent = imageDao.getRecent();
    return (recent != null) ? ok(recent) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  /**
   * Constructs a HTTP response with the image with id 'id'.
   *
   * @param id Long id for Image
   * @return 200 with the image with id 'id' on success, 404 on error
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity get(@PathVariable Long id) {
    Image image = imageDao.get(id);
    return (image != null) ? ok(image) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  /**
   * Constructs a HTTP response with gps locations of the four corners of an image based on Geotag index 0 - top
   * left; index 1 - top right; index 2 - bottom left; index 3 - bottom right;
   *
   * @param id Long id for Image for which the client wants geotag information
   * @return 200 with the gps locations of the four corners of the image with id 'id' based on Geotag, 404 on error
   */
  @RequestMapping(value = "/geotag/{id}", method = RequestMethod.GET)
  public ResponseEntity getGeotagCoordinates(@PathVariable Long id) {
    Image image = imageDao.get(id);
    if (image == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image with given id doesn't exist");
    }
    Map<String, Object> locations = image.getLocations();
    return (locations != null) ? ok(locations) : ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body("The geotag information doesn't exist yet");
  }

  /**
   * Constructs an HTTP response with the given filename.
   *
   * @param file String filename for the requested image file
   * @return 200 with the file with the given filename on success, 500 when error finding or reading
   * the provided file, or 404 when the provided (image) file does not exist
   */
  @RequestMapping(value = "/file/{file}", method = RequestMethod.GET)
  public ResponseEntity getFile(@PathVariable String file) {
    File image = FileUtils.getFile(planeImageDir + file);
    if (image.exists()) {
      HttpHeaders headers = new HttpHeaders();
      InputStream in = null;
      try {
        in = new FileInputStream(planeImageDir + file);
      } catch (FileNotFoundException e) {
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("File not found: " + planeImageDir + file);
      }

      byte[] media = null;
      try {
        media = IOUtils.toByteArray(in);
      } catch (Exception e) {
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error reading file: " + planeImageDir + file);
      }
      headers.setCacheControl(CacheControl.noCache().getHeaderValue());

      ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
      return responseEntity;
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  /**
   * Gets the imageFile from a valid body in the form of a File object.
   *
   * @param file the MultipartFile array of files
   * @return the image file
   */
  private File getImageFile(MultipartFile file) throws IOException {
    File convFile = new File(file.getOriginalFilename());
    convFile.createNewFile();
    FileOutputStream fos = new FileOutputStream(convFile);
    fos.write(file.getBytes());
    fos.close();
    return convFile;
  }

  /**
   * Creates an Image on our server given the request. Constructs an HTTP response with the
   * json of the image that was created. Option to include custom file name in json.
   *
   * @param jsonString the json part of the multipart request as a String
   * @param file       the file of the multipart request
   * @return 200 with the uploaded image on success, 400 when request parts are missing or
   * if the requset json is invalid, or 500 on errors converting file to an image and saving
   * the image in the db
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity upload(@RequestParam("json") String jsonString,
                               @RequestPart("files") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return badRequest().body("Missing image file");
    }
    if (jsonString == null || jsonString.isEmpty()) {
      return badRequest().body("Missing json");
    }

    ObjectNode json;

    try {
      json = (ObjectNode) mapper.readTree(jsonString);
    } catch (Exception e) {
      return badRequest().body("Json part invalid: " + e + " \nReceived: " + jsonString);
    }

    if (json.get("id") != null) {
      return badRequest().body("Don't put id in json of image POST request");
    }

    if (json.get("timestamp") == null) {
      return badRequest().body("Json part must include timestamp field");
    }

    /*if (json.get("fov") == null) {
      return badRequest().body("Json part must include fov field");
    }*/

    if (json.get("imgMode") == null) {
      return badRequest().body("Json part must include imgMode");
    }

    if (json.get("telemetry") == null) {
      return badRequest().body("Json part must include telemetry");
    }

    if (json.get("telemetry").get("gps") == null) {
      return badRequest().body("Json part must include gps within telemetry");
    }

    if (json.get("telemetry").get("gps").get("latitude") == null) {
      return badRequest().body("Json part must include latitude within gps");
    }

    if (json.get("telemetry").get("gps").get("longitude") == null) {
      return badRequest().body("Json part must include longitude within gps");
    }

    if (json.get("telemetry").get("gimOrt") == null) {
      return badRequest().body("Json part must include gimOrt within telemetry");
    }

    if (json.get("telemetry").get("gimOrt").get("pitch") == null) {
      return badRequest().body("Json part must include pitch within gimOrt");
    }

    if (json.get("telemetry").get("gimOrt").get("roll") == null) {
      return badRequest().body("Json part must include roll within gimOrt");
    }

    if (json.get("telemetry").get("altitude") == null) {
      return badRequest().body("Json part must include altitude within telemetry");
    }

    if (json.get("telemetry").get("planeYaw") == null) {
      return badRequest().body("Json part must include planeYaw within telemetry");
    }

    if (json.size() > 4) {
      return badRequest().body("Json part contains invalid field");
    }

    // after this part, the body has been validated
    try {
      json = (ObjectNode) mapper.readTree(jsonString);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when parsing json from request: \n" + e);
    }

    Image i;
    try {
      i = mapper.treeToValue(json, Image.class);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when converting json to Image instance: \n" + e);
    }

    File imageFile;
    try {
      imageFile = getImageFile(file);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when extracting image from request: \n" + e);
    }

    String contentType;
    try {
      contentType = Files.probeContentType(imageFile.toPath());
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when parsing contentType for image file: \n" + e);
    }

    // this is necessary because the Files.probeContentType method above
    // does not recognize the file type for files sent from Linux systems
    if (contentType == null) {
      contentType = "image/jpeg"; // default image content type
    }
    if (!contentType.startsWith("image")) {
      return badRequest().body("expected an image as a filePart");
    }
    String imageExtension = contentType.split("\\/")[1];
    // set the filename to the timestamp of the image
    String imageFileName = String.format("%d", i.getTimestamp().getTime());
    imageFileName += "." + imageExtension;
    i.setLocalImageUrl(planeImageDir + imageFileName);

    // store the image locally
    try {
      FileUtils.moveFile(imageFile, FileUtils.getFile(planeImageDir + imageFileName));
    } catch (FileExistsException e) {
      return badRequest().body("File with timestamp already exists");
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when moving image file: \n" + e);
    }

    i.setImageUrl("/api/v1/image/file/" + imageFileName);

    i.setFov(new FOV(60.0, 60.0));

    imageDao.create(i);

    // TODO: uncomment the following once clients are implemented
    // imageClient.process(i);

    return ok(i);
  }

  /**
   * Ensures the provided image has non-null telemetry data
   *
   * @param i the image to be checked
   * @return the possibly modified image
   */
  private Image defaultValues(Image i) throws Exception {
    Image.ImgMode DEFAULT_IMAGE_MODE = Image.ImgMode.FIXED;
    double DEFAULT_LATITUDE = 42.4440;
    double DEFAULT_LONGITUDE = 76.5019;
    double DEFAULT_ALTITUDE = 100.0;
    double DEFAULT_PLANE_YAW = 0.0;
    double DEFAULT_GIMBAL_PITCH = 0.0;
    double DEFAULT_GIMBAL_ROLL = 0.0;
    FOV DEFAULT_IMAGE_FOV = new FOV(60.0, 60.0);

    if (i.getImgMode() == null) {
      i.setImgMode(DEFAULT_IMAGE_MODE);
    }

    if (i.getTelemetry() == null) {
      Telemetry t = new Telemetry(
          new GpsLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
          DEFAULT_ALTITUDE,
          DEFAULT_PLANE_YAW,
          new GimbalOrientation(DEFAULT_GIMBAL_PITCH, DEFAULT_GIMBAL_ROLL)
      );
      i.setTelemetry(t);
    } else {
      Telemetry t = i.getTelemetry();
      if (t.getGps() == null) {
        t.setGps(new GpsLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE));
      } else {
        GpsLocation g = t.getGps();
        if ((Double) g.getLatitude() == null) {
          g.setLatitude(DEFAULT_LATITUDE);
        }

        if ((Double) g.getLongitude() == null) {
          g.setLongitude(DEFAULT_LONGITUDE);
        }
      }

      if (t.getGimOrt() == null) {
        t.setGimOrt(new GimbalOrientation(DEFAULT_GIMBAL_PITCH, DEFAULT_GIMBAL_ROLL));
      } else {
        GimbalOrientation g = t.getGimOrt();
        if ((Double) g.getPitch() == null) {
          g.setPitch(DEFAULT_GIMBAL_PITCH);
        }

        if ((Double) g.getRoll() == null) {
          g.setRoll(DEFAULT_GIMBAL_ROLL);
        }
      }

      if ((Double) t.getAltitude() == null) {
        t.setAltitude(DEFAULT_ALTITUDE);
      }

      if ((Double) t.getPlaneYaw() == null) {
        t.setPlaneYaw(DEFAULT_PLANE_YAW);
      }
    }
    if (i.getFov() == null) {
      i.setFov(DEFAULT_IMAGE_FOV);
    }

    if (i.getTelemetry() == null) {
      Telemetry t = new Telemetry(
          new GpsLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
          DEFAULT_ALTITUDE,
          DEFAULT_PLANE_YAW,
          new GimbalOrientation(DEFAULT_GIMBAL_PITCH, DEFAULT_GIMBAL_ROLL)
      );
      i.setTelemetry(t);
    } else {
      Telemetry t = i.getTelemetry();
      if (t.getGps() == null) {
        t.setGps(new GpsLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE));
      } else {
        GpsLocation g = t.getGps();
        if ((Double) g.getLatitude() == null) {
          g.setLatitude(DEFAULT_LATITUDE);
        }

        if ((Double) g.getLongitude() == null) {
          g.setLongitude(DEFAULT_LONGITUDE);
        }
      }

      if (t.getGimOrt() == null) {
        t.setGimOrt(new GimbalOrientation(DEFAULT_GIMBAL_PITCH, DEFAULT_GIMBAL_ROLL));
      } else {
        GimbalOrientation g = t.getGimOrt();
        if ((Double) g.getPitch() == null) {
          g.setPitch(DEFAULT_GIMBAL_PITCH);
        }

        if ((Double) g.getRoll() == null) {
          g.setRoll(DEFAULT_GIMBAL_ROLL);
        }
      }

      if ((Double) t.getAltitude() == null) {
        t.setAltitude(DEFAULT_ALTITUDE);
      }

      if ((Double) t.getPlaneYaw() == null) {
        t.setPlaneYaw(DEFAULT_PLANE_YAW);
      }
    }

    return i;
  }

  /**
   * Dummy creates an Image given the request body json. This means that it does not query for the
   * telemetry data or gimbal state. Constructs a HTTP response with the json of the image that was
   * created
   *
   * @return 200 with the uploaded image on success, 400 if id in in the request or 500 parsing request
   * json, converting json to an Image instance, or adding default values
   */
  @RequestMapping(value = "/dummy", method = RequestMethod.POST)
  public ResponseEntity dummyCreate(@RequestBody Image i) {
    if (i.getId() != null) {
      return badRequest().body("Don't put id in json of image POST request");
    }
    i.setImageUrl("/api/v1/image/dummy");

    try {
      defaultValues(i);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when adding default values to Image instance: \n" + e);
    }

    imageDao.create(i);
    return ok(i);
  }
}
