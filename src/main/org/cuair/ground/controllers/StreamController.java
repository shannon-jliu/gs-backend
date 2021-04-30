package org.cuair.ground.controllers;

import org.cuair.ground.util.Flags;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.TimeUnit;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;

import java.io.File;
import java.util.stream.Stream;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.*;
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
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.InterruptedException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;
import java.awt.EventQueue;

@CrossOrigin
@RestController
@RequestMapping("value = /stream")
public class StreamController {

  /** String path to the folder where all the images are stored */
  private String streamSegmentDir = Flags.STREAM_CLIP_DIR;

  private static Pipeline pipeline;

  @PostConstruct
  public void init() {

    configurePaths();

    Gst.init(Version.BASELINE, "BasicPipeline");

    pipeline = (Pipeline) Gst.parseLaunch(Flags.PIPELINE_COMMAND);

    pipeline.play();

    Gst.main();
  }

  public static void configurePaths() {
    String gstPath = System.getProperty("gstreamer.path", "/Library/Frameworks/GStreamer.framework/Libraries/");
    if (!gstPath.isEmpty()) {
      String jnaPath = System.getProperty("jna.library.path", "").trim();
      if (jnaPath.isEmpty()) {
        System.setProperty("jna.library.path", gstPath);
      } else {
        System.setProperty("jna.library.path", jnaPath + File.pathSeparator + gstPath);
      }
    }
  }

  @RequestMapping(value = "/playlist", method = RequestMethod.GET)
  public ResponseEntity getPlaylist() {
    Path path = Paths.get(streamSegmentDir + "playlist.m3u8");
    Resource resource = null;
    try {
      resource = new UrlResource(path.toUri());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }

  /**
   *
   */
  @RequestMapping(value = "/{segment}", method = RequestMethod.GET)
  public ResponseEntity getSegment(@PathVariable String segment) {
    Path path = Paths.get(streamSegmentDir + segment);
    Resource resource = null;
    try {
      resource = new UrlResource(path.toUri());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }

  /**
   *
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
    Path path = Paths.get(streamSegmentDir + fileName);
    try {
      Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return ok("The file has been saved");
  }
}
