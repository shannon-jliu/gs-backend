package org.cuair.ground.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TimestampDatabaseAccessor;
import org.cuair.ground.models.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.web.multipart.support.*;
import org.springframework.web.multipart.*;
import org.springframework.util.MultiValueMap;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.apache.commons.io.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.springframework.http.CacheControl;

import org.springframework.beans.factory.annotation.Value;

/** Contains all the callbacks for all the public api endpoints for the Image  */
@CrossOrigin
@RestController
@RequestMapping(value = "/image")
public class ImageController {
    /** Database accessor object for image database */
    private TimestampDatabaseAccessor imageDao = (TimestampDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.TIMESTAMP_DATABASE_ACCESSOR, Image.class);

    /** String path to the folder where all the images are stored */
    @Value("${plane.image.dir}") private String PLANE_IMAGE_DIR;

    /** String path to the folder where all the images are backed up */
    @Value("${plane.image.backup.dir}") private String PLANE_IMAGE_BACKUP_DIR;

    private ObjectMapper mapper = new ObjectMapper();

    /** A logger */
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    /**
     * Constructs an HTTP response with all the images.
     *
     * @return HTTP response
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Image>> getAll() {
        return ResponseEntity.ok(imageDao.getAll());
    }

    /**
     * Constructs an HTTP response with the most recent image that was captured by the plane.
     *
     * @return HTTP response
     */
    @RequestMapping(value = "/recent", method = RequestMethod.GET)
    public ResponseEntity<Image> getRecent() {
        Image recent = (Image) imageDao.getRecent();
        return (recent != null) ? ResponseEntity.ok(recent) : ResponseEntity.noContent().build();
    }

    /**
     * Constructs a HTTP response with the image with id 'id'.
     *
     * @param id Long id for Image
     * @return HTTP response
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Image> get(@PathVariable Long id) {
        Image image = (Image) imageDao.get(id);
        return (image != null) ? ResponseEntity.ok(image) : ResponseEntity.noContent().build();
    }

    /**
     * Constructs a HTTP response with the image file with url `file`
     *
     * @param file String image url for the File we are extracting
     * @return HTTP response
     */
    @RequestMapping(value = "/file/{file}", method = RequestMethod.GET)
    public ResponseEntity getFile(@PathVariable String file) {
        // TODO: Is this necessary? It will be caught in exception FileNotFoundException below
        File image = FileUtils.getFile(PLANE_IMAGE_DIR + file);
        if (image.exists()) {
            HttpHeaders headers = new HttpHeaders();
            InputStream in = null;
            try {
                in = new FileInputStream(PLANE_IMAGE_DIR + file);
            } catch (FileNotFoundException e) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File not found: " + PLANE_IMAGE_DIR + file);
            }

            byte[] media = null;
            try {
                media = IOUtils.toByteArray(in);
            } catch (IOException e) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading file: " + PLANE_IMAGE_DIR + file);
            }
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());

            ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
            return responseEntity;
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks to see if the body of a call to /image is valid. Returns a badRequest if not, null if it
     * is a proper call.
     *
     * @param req the request
     * @param isPost true if it is a POST (create) action, false if it is a PUT (update) action
     * @return null if the body is valid, otherwise a badRequest
     */
    private CompletableFuture<ResponseEntity> validateRequestBody(MultipartFile[] files, String jsonString, boolean isPost) {
        if (files == null && jsonString == null) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request should provide multipart/form-data")
            );
        }

        String reqType = isPost ? "POST" : "PUT";

        if (files.length == 0) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing image file in image " + reqType + " request")
            );
        }
        if (files.length != 1) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Should have only one file in image " + reqType + " request")
            );
        }
        if (jsonString == null) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing json in image " + reqType + " request")
            );
        }

        ObjectNode json = null;

        try {
            json = (ObjectNode) mapper.readTree(jsonString);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Json part invalid: " + e + " \nReceived: " + jsonString)
            );
        }

        // POST-specific requirements, maybe better way to do this than just nested if statement?
        if (isPost) {
            if (json.get("id") != null) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't put id in json of image POST request")
                );
            }

            if (json.get("timestamp") == null) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Json part must include timestamp field")
                );
            }

            // TODO: But we have timestamp and imgMode in the json part
            // if (json.size() > 1) {
            //     return CompletableFuture.completedFuture(
            //         ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Json part contains invalid field")
            //     );
            // }
        }
        return null;
    }

    /**
     * Gets the JSON of a verified body. Precondition is the the body is valid (see above method).
     *
     * @param req the valid request
     * @return the ObjectNode representing the JSON of the body
     */
    private ObjectNode getJSON(String jsonString) throws IOException {
        return (ObjectNode) mapper.readTree(jsonString);
    }

    /**
     * Gets the File file from a MultipartFile file.
     *
     * @param file MultipartFile to be converted to a File
     * @return the file that has been converted to File
     */
    private File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    /**
     * Gets the imageFile from a valid body in the form of a File object.
     *
     * @param req the valid request
     * @return the image file
     */
    private File getImageFile(MultipartFile[] files) throws IOException {
        return convert(files[0]);
    }

    /**
     * Creates an Image on our server given the request. Constructs an HTTP response with the
     * json of the image that was created. Option to include custom file name in json.
     *
     * @param req the request
     * @return an HTTP response
     */
    @RequestMapping(method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity> create(MultipartHttpServletRequest request) {
        Map<String, String[]> formData = request.getParameterMap();
        String jsonString = formData.get("jsonString")[0];
        MultipartFile[] files = {request.getFile("files")};
        // check if request is valid
        CompletableFuture<ResponseEntity> validate = validateRequestBody(files, jsonString, true);
        if (validate != null) {
            return validate;
        }

        ObjectNode json = null;
        try {
            json = getJSON(jsonString);
        } catch (IOException e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e)
            );
        }

        Image i = null;
        try {
            i = mapper.treeToValue(json, Image.class);
        } catch (JsonProcessingException e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when convert json to Image instance: \n" + e)
            );
        }

        String name = null;
        if (json.get("name") != null) {
            name = json.remove("name").asText();
        }

        // set image file name
        String imageFileName;
        if (name != null) {
            imageFileName = name;
        } else {
            imageFileName = String.format("%d", i.getTimestamp().getTime());
        }

        File imageFile = null;
        try {
            imageFile = getImageFile(files);
        } catch (IOException e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when extracting image from request: \n" + e)
            );
        }

        String contentType;
        try {
            contentType = Files.probeContentType(imageFile.toPath());
        } catch (IOException e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing contentType for image file: \n" + e)
            );
        }

        // this is necessary because the Files.probeContentType method above
        // does not recognize the file type for files sent from Linux systems
        if (contentType == null) contentType = "image/jpeg"; // default image content type
        if (!contentType.startsWith("image")) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("expected an image as a filePart")
            );
        }
        String imageExtension = contentType.split("\\/")[1];
        imageFileName += "." + imageExtension;
        i.setLocalImageUrl(PLANE_IMAGE_DIR + imageFileName);

        // store the image locally
        try {
            FileUtils.moveFile(imageFile, FileUtils.getFile(PLANE_IMAGE_DIR + imageFileName));
        } catch (FileExistsException e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File with timestamp already exists")
            );
        } catch (IOException e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when moving image file: \n" + e)
            );
        }

        i.setImageUrl("/api/v1/image/file/" + imageFileName);

        imageDao.create(i);
        // imageClient.process(i);
        return CompletableFuture.completedFuture(ResponseEntity.ok(i));
    }

    /**
     * Returns JSON of gps locations of the four corners of an image based on Geotag index 0 - top
     * left; index 1 - top right; index 2 - bottom left; index 3 - bottom right;
     *
     * @return an HTTP response
     */
    @RequestMapping(value = "/geotag/{id}", method = RequestMethod.GET)
    public ResponseEntity getGeotagCoordinates(@PathVariable Long id) {
        if (id == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image ID is null");
        Image i = (Image) imageDao.get(id);
        if (i != null) {
            return ResponseEntity.ok(i.getLocations());
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Dummy creates an Image given the request body json. This means that it does not query for the
     * telemetry data or gimbal state. Constructs a HTTP response with the json of the image that was
     * created
     *
     * @param req the request
     * @return an HTTP response
     */
    @RequestMapping(value = "/dummy", method = RequestMethod.POST)
    public ResponseEntity dummyCreate(MultipartHttpServletRequest request) {
        Map<String, String[]> formData = request.getParameterMap();
        String jsonString = formData.get("jsonString")[0];
        MultipartFile[] files = {request.getFile("files")};

        ObjectNode json = null;
        try {
            json = getJSON(jsonString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
        }

        Image i = null;
        try {
            i = mapper.treeToValue(json, Image.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when convert json to Image instance: \n" + e);
        }

        if (i.getId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Don't put id in json of image POST request");
        }
        imageDao.create(i);
        return ResponseEntity.ok(i);
    }

}
