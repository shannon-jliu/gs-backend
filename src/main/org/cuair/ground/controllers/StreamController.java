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
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ImageDatabaseAccessor;
import org.cuair.ground.models.Image;
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
@RequestMapping(value = "/stream")
public class StreamController {
  /** String path to the folder where all the images are stored */
  private String streamImageDir = Flags.STREAM_IMAGE_DIR;

  /**
   *
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity get() {
    return ok("We get it");
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
   *
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity incoming(@RequestPart("file") MultipartFile file) {
    File imageFile;
    try {
      imageFile = getImageFile(file);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when parsing contentType for image file: \n" + e);
    }

    // store the image locally
    try {
      FileUtils.moveFile(imageFile,
          FileUtils.getFile(streamImageDir + System.currentTimeMillis() + ".png"));
    } catch (FileExistsException e) {
      return badRequest().body("File with timestamp already exists");
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when moving image file: \n" + e);
    }


    // LOGIC FOR STARTING NEW MUX/VIDEO


    return ok("We have received and saved the image");
  }
}
