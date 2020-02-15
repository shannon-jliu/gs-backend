package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.badRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TimestampDatabaseAccessor;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.beans.factory.annotation.Value;
import org.cuair.ground.util.Flags;

/** Contains all the callbacks for all the public api endpoints for the Image  */
@CrossOrigin
@RestController
@RequestMapping(value = "/image")
public class ImageController {
    /** Database accessor object for image database */
    private TimestampDatabaseAccessor imageDao = (TimestampDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.TIMESTAMP_DATABASE_ACCESSOR, Image.class);

    /** String path to the folder where all the images are stored */
    private String PLANE_IMAGE_DIR = Flags.PLANE_IMAGE_DIR;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    // required for obtaining this server's context to grab the location of the image file
    private ServletContext context;

    /**
     * Constructs an HTTP response with all the images.
     *
     * @return HTTP response
     */
    @RequestMapping(value = "/all/{id}", method = RequestMethod.GET)
    public ResponseEntity getAllAfterId(@PathVariable Long id) {
        List<Image> images = new ArrayList();
        Image recent = (Image) imageDao.getRecent();
        if (recent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            Long mostRecentId = recent.getId();
            for (Long index = id+1; index <= mostRecentId; index++) {
                images.add((Image) imageDao.get(index));
            }
            return ok(images);
        }
    }

    /**
     * Constructs an HTTP response with the most recent image that was captured by the plane.
     *
     * @return HTTP response
     */
    @RequestMapping(value = "/recent", method = RequestMethod.GET)
    public ResponseEntity getRecent() {
        Image recent = (Image) imageDao.getRecent();
        return (recent != null) ? ok(recent) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Constructs a HTTP response with the image with id 'id'.
     *
     * @param id Long id for Image
     * @return HTTP response
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity get(@PathVariable Long id) {
        Image image = (Image) imageDao.get(id);
        return (image != null) ? ok(image) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Gets the JSON of a verified body. Precondition is that the body is valid (see above method).
     *
     * @param jsonString the valid JSON body represented as a string
     * @return the ObjectNode representing the JSON of the body
     */
    private ObjectNode getJSON(String jsonString) throws IOException {
        return (ObjectNode) mapper.readTree(jsonString);
    }

    /**
     * Gets the imageFile from a valid body in the form of a File object.
     *
     * @param file the MultipartFile array of files
     * @return the image file
     */
    private File getImageFile(MultipartFile file) throws IOException {
      // save file to temp dir created by Spring context
      File imgFile = new File(context.getRealPath(file.getOriginalFilename()));
      file.transferTo(imgFile);
      return imgFile;
    }

    /**
     * Creates an Image on our server given the request. Constructs an HTTP response with the
     * json of the image that was created. Option to include custom file name in json.
     *
     * @param jsonString the json part of the multipart request as a String
     * @param file the file of the multipart request
     * @return an HTTP response
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity upload(@RequestPart("json") String jsonString,
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

        if (json.get("telemetry").get("altitude") == null) {
            return badRequest().body("Json part must include altitude within telemetry");
        }

        if (json.get("telemetry").get("planeYaw") == null) {
            return badRequest().body("Json part must include planeYaw within telemetry");
        }

        if (json.size() > 6) {
            return badRequest().body("Json part contains invalid field");
        }

        // after this part, the body has been validated
        try {
            json = getJSON(jsonString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
        }

        Image i;
        try {
            i = mapper.treeToValue(json, Image.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when converting json to Image instance: \n" + e);
        }

        // set the filename to the timestamp of the image
        String imageFileName = String.format("%d", i.getTimestamp().getTime());

        File imageFile;
        try {
            imageFile = getImageFile(file);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when extracting image from request: \n" + e);
        }

        String contentType;
        try {
            contentType = Files.probeContentType(imageFile.toPath());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing contentType for image file: \n" + e);
        }

        // this is necessary because the Files.probeContentType method above
        // does not recognize the file type for files sent from Linux systems
        if (contentType == null) contentType = "image/jpeg"; // default image content type
        if (!contentType.startsWith("image")) {
            return badRequest().body("expected an image as a filePart");
        }
        String imageExtension = contentType.split("\\/")[1];
        imageFileName += "." + imageExtension;
        i.setLocalImageUrl(PLANE_IMAGE_DIR + imageFileName);

        // store the image locally
        try {
            FileUtils.moveFile(imageFile, FileUtils.getFile(PLANE_IMAGE_DIR + imageFileName));
        } catch (FileExistsException e) {
            return badRequest().body("File with timestamp already exists");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when moving image file: \n" + e);
        }

        i.setImageUrl("/api/v1/image/file/" + imageFileName);

        imageDao.create(i);

        // TODO: uncomment the following once clients are implemented
        // imageClient.process(i);

        return ok(i);
    }

    /**
     * Ensures the provided image has non-null telemetry data
     *
     * @param i the image to be checked
     * @return the (possibly modified) image
     */
    private Image defaultValues(Image i) throws Exception {
        Double DEFAULT_LATITUDE = 42.4440;
        Double DEFAULT_LONGITUDE = 76.5019;
        Double DEFAULT_ALTITUDE = 100.0;
        Double DEFAULT_PLANE_YAW = 0.0;

        if (i.getTelemetry() == null) {
            Telemetry t = new Telemetry(new GpsLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), DEFAULT_ALTITUDE, DEFAULT_PLANE_YAW);
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
     * @param jsonString the json request for this dummy create, as a string
     * @return an HTTP response
     */
    @RequestMapping(value = "/dummy", method = RequestMethod.POST)
    public ResponseEntity dummyCreate(String jsonString) {
        ObjectNode json;
        try {
            json = getJSON(jsonString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
        }

        Image i;
        try {
            i = mapper.treeToValue(json, Image.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when converting json to Image instance: \n" + e);
        }

        if (i.getId() != null) {
            return badRequest().body("Don't put id in json of image POST request");
        }
        i.setImageUrl("/api/v1/image/dummy");

        try {
            defaultValues(i);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when adding default values to Image instance: \n" + e);
        }

        imageDao.create(i);
        return ok(i);
    }

}
