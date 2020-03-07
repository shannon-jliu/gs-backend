package org.cuair.ground.util;

public class Flags {

  /* Autopilot */
  
  public static String AUTOPILOT_GROUND_IP = "192.168.0.22";

  public static String AUTOPILOT_GROUND_PORT = "8001";

  public static String AUTOPILOT_COVERAGE = "/ground/api/v3/distributed/geotag";

  public static String AUTOPILOT_GROUND_MDLC_ROIS = "/ground/api/v3/distributed/mdlc";

  public static String AUTOPILOT_GROUND_ADLC_ROIS = "/ground/api/v3/distributed/adlc";

  /* Plane servers */

  public static boolean PRINT_CLIENT_LOGS = true;

  public static String OBC_IP = "192.168.0.21";

  public static String CAM_GIM_PORT = "5000";

  public static String AIR_API_PORT = "5001";

  public static String AIRDROP_PORT = "5002";

  public static String SET_CAM_GIM_MODE_SETTINGS_ROUTE = "/api/mode";

  public static String SET_AIRDROP_SETTINGS_ROUTE = "/v1/airdrop/setting";

  public static String GET_CAM_GIM_MODE_SETTINGS_ROUTE = "/api/state";

  public static String GET_AIRDROP_SETTINGS_ROUTE = "/v1/airdrop/state";

  /* Interop */

  public static boolean CUAIR_INTEROP_REQUESTS = true;

  public static String MISSION_INFO = "/api/missions";

  public static int MISSION_ID = 1;

  public static String TARGET_ROUTE = "/api/odlcs";

  public static int TARGETLOGGER_DELAY = 30000;

  public static String INTEROP_TARGET_DIR = "images/";

  /* Auth */

  public static boolean AUTH_ENABLED = false;

  public static String DEFAULT_USER = "<NO_USER>";

  /* Targets and Images */

  public static String PLANE_IMAGE_DIR = "images/";

  public static String DEFAULT_EMERGENT_TARGET_DESC = "Lost hiker laying down on ground.";

  /* Geotag */

  public static double IMAGE_WIDTH = 4912.0;

  public static double IMAGE_HEIGHT = 3684.0;

  public static double FOV_HORIZONTAL_RADIANS = 0.7328394987;

  public static double FOV_VERTICAL_RADIANS = 0.560476881;

  public static boolean CUAIR_GEOTAG_MUTABLE = true;

  /* Airdrop */

  public static double CUAIR_AIRDROP_THRESHOLD = 150.0;
}
