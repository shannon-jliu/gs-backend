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
}
