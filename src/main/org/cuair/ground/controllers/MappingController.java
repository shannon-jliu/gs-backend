package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.cuair.ground.util.Flags;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all the callbacks for all the public api endpoints for the mapping
 * task.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/mapping")
public class MappingController {

  // Holds the most recently submitted map
  private static byte[] submittedMap;

  /**
   * Defines endpoint for Intelligent Systems to submit an image of a mapping.
   *
   * @param file the file of the multipart request
   *
   * @return 200 with the uploaded mapping image on success, 400 when the map file
   *         doesn't exist or
   *         is too large, or 500 if communication with interop fails or the file
   *         cannot be parsed.
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity submitMapping(@RequestPart("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return badRequest().body("Missing mapping image file");
    }

    // Parse provided file into bytes
    byte[] rawContent;
    try {
      rawContent = file.getBytes();
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Could not parse map file.");
    }

    // Check to see if the image is small enough -> otherwise interop will fail
    if (rawContent.length > Math.pow(10, 6))
      return badRequest().body("Mapping image file is larger than one megabyte.");

    submittedMap = rawContent;
    return ok(rawContent);
  }

  /**
   * Defines endpoint for Intelligent Systems to submit the raw byte content of a
   * mapping image.
   *
   * @param rawContent a byte array corresponding to the map
   *
   * @return 200 with the uploaded mapping image on success, 400 when the byte
   *         array doesn't exist,
   *         or 500 if communication with interop fails.
   */
  @RequestMapping(value = "/bytes", method = RequestMethod.POST)
  public ResponseEntity submitMapping(@RequestPart("bytes") byte[] rawContent) {

    Logger logger = LoggerFactory.getLogger(MappingController.class);
    logger.info("raw content is " + rawContent);

    if (rawContent == null) {
      return badRequest().body("Missing mapping data");
    }

    // Check to see if the image is small enough -> otherwise interop will fail
    if (rawContent.length > Math.pow(10, 6)) {
      logger.info("too big " + rawContent.length);
      return badRequest().body("Mapping image file is larger than one megabyte.");
    }

    submittedMap = rawContent;
    return ok(rawContent);
  }


}
