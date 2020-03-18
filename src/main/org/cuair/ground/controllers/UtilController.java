package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.ok;

import io.ebean.Ebean;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.cuair.ground.util.Flags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Contains all the callbacks for all the public api endpoints for the User */
@CrossOrigin
@RestController
@RequestMapping(value = "/util")
public class UtilController {
  private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

  /** String path to the folder where all the images are stored */
  private String planeImageDir = Flags.PLANE_IMAGE_DIR;

  /**
   * @return 200 if the database is cleared successfully, otherwise 400 with a descriptive error message
   */
  @RequestMapping(value = "/clear_mdlc", method = RequestMethod.POST)
  public ResponseEntity clearMdlc() {
    String[] names = {
        "telemetry",
        "emergent_target",
        "geotag",
        "alphanum_target",
        "image",
        "assignment",
        "alphanum_target_sighting",
        "emergent_target_sighting"
    };
    for (String name : names) {
      String sql = "TRUNCATE " + name + " RESTART IDENTITY CASCADE";
      Ebean.createSqlUpdate(sql).execute();
    }

    try {
      FileUtils.cleanDirectory(FileUtils.getFile(planeImageDir));
    } catch (IOException e) {
      logger.error(planeImageDir + " could not be cleared\n");
    }

    logger.info("The MDLC-related tables and image directory have been cleared\n");

    return ok().build();
  }
}
