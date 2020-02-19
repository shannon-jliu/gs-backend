package org.cuair.ground.util;

public class Flags {
  /** Special username */
  public static String MDLC_OPERATOR_USERNAME = "operator";
  
  // Image directory
  public static String PLANE_IMAGE_DIR = "images/";

  // Constants
  public static double FOV_HORIZONTAL_RADIANS = 0.7328394987;
  public static double FOV_VERTICAL_RADIANS = 0.560476881;
  public static double IMAGE_WIDTH = 4912.0;
  public static double IMAGE_HEIGHT = 3684.0;

  // Flags
  public static boolean AUTH_ENABLED = false;
  public static boolean CUAIR_INTEROP_REQUESTS = false;
  public static boolean CUAIR_GEOTAG_MUTABLE = false;

  // Security
  public static String DEFAULT_USER = "<NO_USER>";

  /** Default username if no username specified as part of request */
  public static String DEFAULT_USERNAME = "<NO_USER>";

  /** Allow multiple users on a single IP if set to true. */
  public static boolean ENABLE_MULTIPLE_USERS_PER_IP = true;

  /** Allows clients to create users if true. Otherwise, all will use default user */
  public static boolean USERS_ENABLED = true;
}
