package org.cuair.ground.util;

public class Flags {
  /** Special username */
  public static String MDLC_OPERATOR_USERNAME = "operator";
  public static String INTSYS_TAGGER_USERNAME = "intsys";

  /** Image Directory */
  public static String PLANE_IMAGE_DIR = "images/";

  /** Test image directory */
  public static String TEST_IMAGE_DIR = "src/test/java/org/cuair/ground/controllers/test_images/";

  /** Constants */
  public static double CAM_SENSOR_WIDTH = 23.2; // in mm
  public static double CAM_SENSOR_HEIGHT = 15.4; // in mm
  public static double RAW_IMAGE_WIDTH = 5456;
  public static double RAW_IMAGE_HEIGHT = 3632;
  public static double FRONTEND_IMAGE_WIDTH = RAW_IMAGE_WIDTH; // 1900
  public static double FRONTEND_IMAGE_HEIGHT = RAW_IMAGE_HEIGHT; // 1263

  public static String DEFAULT_EMERGENT_TARGET_DESC = "A lost hiker with a water bottle.";

  public static int MISSION_NUMBER = 2;

  /** Default username if no username specified as part of request */
  public static String DEFAULT_USERNAME = "<NO_USER>";

  /** Allow multiple users on a single IP if set to true. */
  public static boolean ENABLE_MULTIPLE_USERS_PER_IP = true;

  /**
   * Allows clients to create users if true. Otherwise, all will use default user
   */
  public static boolean USERS_ENABLED = true;

  /** Autopilot */
  public static String AUTOPILOT_GROUND_IP = "192.168.0.4";
  public static String AUTOPILOT_GROUND_PORT = "8001";
  public static String AUTOPILOT_COVERAGE = "/ground/api/v3/distributed/geotag";
  public static String AUTOPILOT_GROUND_MDLC_ROIS = "/ground/api/v3/distributed/mdlc";
  public static String AUTOPILOT_GROUND_ADLC_ROIS = "/ground/api/v3/distributed/adlc";

  /** Plane servers */
  public static boolean PRINT_CLIENT_LOGS = true;
  public static String OBC_IP = "192.168.0.21";
  // change PS_MODES_IP and CAMERA_COMMANDS_IP
  // TODO: delete and deprecate later
  // public static String PS_MODES_IP = "192.168.1.5;"; // "192.168.1.23";
  // public static String GIMBAL_COMMANDS_IP = "192.168.1.24";
  public static String CAMERA_COMMANDS_IP = "192.168.1.3"; // "192.168.1.25"
  public static String CAM_GIM_PORT = "5000";
  public static String SET_CAM_GIM_MODE_SETTINGS_ROUTE = "/api/mode";
  public static String SET_AIRDROP_SETTINGS_ROUTE = "/v1/airdrop/setting";
  public static String GET_CAM_GIM_MODE_SETTINGS_ROUTE = "/api/state";
  public static String GET_AIRDROP_SETTINGS_ROUTE = "/v1/airdrop/state";

  public static String MAIN_CAMERA_COMMANDS_PORT = "4200";
  public static String SET_FOCAL_LENGTH_ROUTE = "/camera/set-zoom-focal-length";
  public static String CONTROL_GIMBAL_ROUTE = "/gimbal/control-gimbal";
  public static String SET_ZOOM_LEVEL_ROUTE = "/camera/set-zoom-level";

  public static String SET_APERTURE_ROUTE = "/camera/set-aperture";

  public static String SET_SHUTTER_SPEED_ROUTE = "/camera/set-shutter-speed";
  public static String SET_EXPOSURE_MODE_ROUTE = "/camera/set-exposure-mode";

  public static String SET_PAN_SEARCH_ROUTE = "/modes/pan-search";
  public static String SET_MANUAL_SEARCH_ROUTE = "/modes/manual-search";
  public static String SET_DISTANCE_SEARCH_ROUTE = "/modes/distance-search";
  public static String SET_TIME_SEARCH_ROUTE = "/modes/time-search";

  public static String CAPTURE_ROUTE = "/camera/capture";

  public static String GET_STATUS_ROUTE = "/camera/get-status";

  /** Streaming */
  public static String STREAM_CLIP_DIR = "src/main/org/cuair/ground/";
  public static int MAX_CAMERAS = 5;
  public static int PORT_START = 5000;
  public static String STREAMING_CAPS = " caps = \"application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264, payload=(int)96\" ! rtph264depay ! decodebin ! videoconvert ! x264enc tune=zerolatency ! mpegtsmux !";

  /** A constant used in the DBSCAN calculation for clustering ROIs */
  public static Double CUAIR_CLUSTERING_EPSILON = 0.0003173611111111856;
}
