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

  /** Allows clients to create users if true. Otherwise, all will use default user */
  public static boolean USERS_ENABLED = true;
}
