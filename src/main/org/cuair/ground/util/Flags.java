package org.cuair.ground.util;

public class Flags {
  /** Special username */
  public static String MDLC_OPERATOR_USERNAME = "operator";

  /** Image Directory */
  public static String PLANE_IMAGE_DIR = "images/";

  /** Test image directory */
  public static String TEST_IMAGE_DIR = "src/test/java/org/cuair/ground/controllers/test_images/";

  /** Constants */
  public static double FOV_HORIZONTAL_RADIANS = 0.7328394987;
  public static double FOV_VERTICAL_RADIANS = 0.560476881;
  public static double IMAGE_WIDTH = 4912.0;
  public static double IMAGE_HEIGHT = 3684.0;

  /** Interop */
  public static boolean CUAIR_INTEROP_REQUESTS = false;
  public static String DEFAULT_EMERGENT_TARGET_DESC = "A lost hiker with a water bottle.";

  /** Default username if no username specified as part of request */
  public static String DEFAULT_USERNAME = "<NO_USER>";

  /** Allow multiple users on a single IP if set to true. */
  public static boolean ENABLE_MULTIPLE_USERS_PER_IP = true;

  /**
   * Allows clients to create users if true. Otherwise, all will use default user
   */
  public static boolean USERS_ENABLED = true;

  /** Autopilot */
  public static String AUTOPILOT_GROUND_IP = "192.168.0.22";
  public static String AUTOPILOT_GROUND_PORT = "8001";
  public static String AUTOPILOT_COVERAGE = "/ground/api/v3/distributed/geotag";
  public static String AUTOPILOT_GROUND_MDLC_ROIS = "/ground/api/v3/distributed/mdlc";
  public static String AUTOPILOT_GROUND_ADLC_ROIS = "/ground/api/v3/distributed/adlc";

  /** Plane servers */
  public static boolean PRINT_CLIENT_LOGS = true;
  public static String OBC_IP = "192.168.0.21";
  public static String CAM_GIM_PORT = "5000";
  public static String SET_CAM_GIM_MODE_SETTINGS_ROUTE = "/api/mode";
  public static String SET_AIRDROP_SETTINGS_ROUTE = "/v1/airdrop/setting";
  public static String GET_CAM_GIM_MODE_SETTINGS_ROUTE = "/api/state";
  public static String GET_AIRDROP_SETTINGS_ROUTE = "/v1/airdrop/state";

  /** Streaming */
  public static String STREAM_CLIP_DIR = "src/main/org/cuair/ground/stream_segments/";
  public static String PIPELINE_COMMAND = "udpsrc port=5000 caps = \"application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264, payload=(int)96\" ! rtph264depay ! decodebin ! videoconvert ! x264enc tune=zerolatency ! mpegtsmux ! hlssink playlist-location=src/main/org/cuair/ground/stream_segments/playlist.m3u8 location=src/main/org/cuair/ground/stream_segments/segment_%05d.ts target-duration=1 playlist-length=0 max-files=0";

  // src/main/org/cuair/ground/stream_segments/segment_%05d.ts

  // "udpsrc port=5000 caps =
  // \"application/x-rtp,media=video,clock-rate=90000,encoding-name=H264,payload=96\"
  // ! rtph264depay ! avdec_h264 ! autovideoconvert ! videoflip method=rotate-180
  // ! autovideoconvert ! hlssink
  // playlist-location=src/main/org/cuair/ground/stream_segments/playlist.m3u8
  // location=src/main/org/cuair/ground/stream_segments/segment_%05d.ts
  // target-duration=5 max-files=0";

  // udpsrc port=5001 caps =
  // "application/x-rtp,media=video,clock-rate=90000,encoding-name=H264,payload=96"
  // ! rtph264depay ! avdec_h264 ! autovideoconvert ! videoflip method=rotate-180
  // ! autovideosink
  /** A constant used in the DBSCAN calculation for clustering ROIs */
  public static Double CUAIR_CLUSTERING_EPSILON = 0.0003173611111111856;
}
