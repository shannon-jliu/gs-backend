package org.cuair.ground.controllers;

import org.cuair.ground.util.Flags;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.TimeUnit;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;

import java.io.File;
import java.util.stream.Stream;

@RequestMapping("/stream")
public class StreamController {

  private static Pipeline pipeline;

  public static void main(String[] args) {
    configurePaths();

    Gst.init(Version.BASELINE, "BasicPipeline", args);

    pipeline = (Pipeline) Gst.parseLaunch("videotestsrc ! autovideosink");

    pipeline.play();

    Gst.main();

  }

  public static void configurePaths() {
    String gstPath =
        System.getProperty("gstreamer.path", "/Library/Frameworks/GStreamer.framework/Libraries/");
    if (!gstPath.isEmpty()) {
      String jnaPath = System.getProperty("jna.library.path", "").trim();
      if (jnaPath.isEmpty()) {
        System.setProperty("jna.library.path", gstPath);
      } else {
        System.setProperty("jna.library.path", jnaPath + File.pathSeparator + gstPath);
      }
    }
  }

}
