package org.cuair.ground.util;

public class Flags {
  /** Image directory */
  public static String PLANE_IMAGE_DIR = "images/";

  /** Special username */
  public static String MDLC_OPERATOR_USERNAME = "operator";

  /** Default username if no username specified as part of request */
  public static String DEFAULT_USERNAME = "<NO_USER>";

  /** Allow multiple users on a single IP if set to true. */
  public static boolean ENABLE_MULTIPLE_USERS_PER_IP = true;

  /** Allows clients to create users if true. Otherwise, all will use default user */
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

  public static String AIR_API_PORT = "5001";

  public static String AIRDROP_PORT = "5002";

  public static String SET_CAM_GIM_MODE_SETTINGS_ROUTE = "/api/mode";

  public static String SET_AIRDROP_SETTINGS_ROUTE = "/v1/airdrop/setting";

  public static String GET_CAM_GIM_MODE_SETTINGS_ROUTE = "/api/state";

  public static String GET_AIRDROP_SETTINGS_ROUTE = "/v1/airdrop/state";
}
