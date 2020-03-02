package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlUpdate;
import io.ebean.SqlRow;
import org.cuair.ground.util.Flags;
import org.apache.commons.io.FileUtils;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains all the callbacks for all the public api endpoints for the User  */
@CrossOrigin
@RestController
@RequestMapping(value = "/util")
public class UtilController {

  /** String path to the folder where all the images are stored */
  private String PLANE_IMAGE_DIR = Flags.PLANE_IMAGE_DIR;

  /** A logger */
  private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

  /**
   *
   * @return 200 if the database is cleared successfully, otherwise 400 with a descriptive error message
   */
  @RequestMapping(value = "/clear", method = RequestMethod.POST)
  public ResponseEntity clearDb() {
    String sql = "SELECT table_name" + " FROM information_schema.tables" + " WHERE table_schema='public'" + " AND table_type='BASE TABLE'";
    SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
    List<SqlRow> list = sqlQuery.findList();
    for (SqlRow row : list) {
        sql = "TRUNCATE " + row.get("table_name") + " CASCADE";
        SqlUpdate delete = Ebean.createSqlUpdate(sql);
        delete.execute();
    }

    try {
        FileUtils.cleanDirectory(FileUtils.getFile(PLANE_IMAGE_DIR));
    } catch (IOException e) {
        logger.error(PLANE_IMAGE_DIR + " could not be cleared\n");
    }

    logger.warn("The database and image directory have been cleared\n");

    return ok().build();
  }
}
