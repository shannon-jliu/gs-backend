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
import org.springframework.core.io.ClassPathResource;
import java.net.MalformedURLException;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;
import java.awt.EventQueue;

/**
 * Controls starting stream download over udp through gstreamer
 * Handles requests from gs-frontend for stream clips
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/stream")
public class StreamController {

  /** String path to the folder where all the images are stored */
  private String streamSegmentDir = Flags.STREAM_CLIP_DIR;

  private static String ConstructPipeline(int i) {
    String start = "udpsrc port=";
    String port = String.valueOf(Flags.PORT_START + i);
    String playlistLocation = String
        .format(" hlssink playlist-location=src/main/org/cuair/ground/stream%d_segments/playlist.m3u8", i);
    String clipsLocation = " location=src/main/org/cuair/ground/stream" + String.valueOf(i) + "_segments/"
        + String.valueOf(i) + "_segment_%05d.ts target-duration=1 playlist-length=0 max-files=0";
    return start + port + Flags.STREAMING_CAPS + playlistLocation + clipsLocation;
  }

  private static List<Pipeline> pipelines = new ArrayList<Pipeline>();

  private void createFolder(int number) {
    String PATH = Flags.STREAM_CLIP_DIR + "stream" + String.valueOf(number) + "_segments";

    File directory = new File(PATH);
    if (!directory.exists()) {
      directory.mkdir();
    }
  }

  /** Initialize stream controller - create and play gst pipelines */
  @PostConstruct
  public void init() {

    configurePaths();

    Gst.init(Version.BASELINE, "BasicPipeline");

    // initialize pipelines and folders
    for (int i = 0; i < Flags.MAX_CAMERAS; i++) {
      createFolder(i);

      pipelines.add((Pipeline) Gst.parseLaunch(ConstructPipeline(i)));
    }

    // play pipelines
    for (int i = 0; i < Flags.MAX_CAMERAS; i++) {
      pipelines.get(i).play();
    }
  }

  /** Configure paths for gstreamer libraries */
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

  /**
   * Constructs an HTTP response with the stream playlist file
   * 
   * @param i stream id
   * @return path to requested playlist
   */
  @RequestMapping(value = "/playlist", method = RequestMethod.GET)
  public ResponseEntity getPlaylist(@RequestParam("number") int i) {
    Path path = Paths.get(String.format("%sstream%d_segments/playlist.m3u8", streamSegmentDir, i));
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
   * Constructs an HTTP response with a stream segment
   *
   * @return path to requested segment
   */
  @RequestMapping(value = "/{segment}", method = RequestMethod.GET)
  public ResponseEntity getSegment(@PathVariable String segment) {
    String[] segmentParts = segment.split("_", 2);
    String playlistNumber = segmentParts[0];
    Path path = Paths.get(String.format("%sstream%s_segments/%s", streamSegmentDir, playlistNumber, segment));
    Resource resource = null;
    try {
      resource = new UrlResource(path.toUri());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }
}
