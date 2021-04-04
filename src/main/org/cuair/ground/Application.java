package org.cuair.ground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.cuair.ground.util.Flags;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;

import java.io.File;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {
  private static Pipeline pipeline;

  public static void main(String[] args) {
    configurePaths();

    Gst.init(Version.BASELINE, "BasicPipeline", args);

    pipeline = (Pipeline) Gst.parseLaunch(Flags.PIPELINE_COMMAND);

    pipeline.play();

    Gst.main();

    SpringApplication.run(Application.class, args);
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
