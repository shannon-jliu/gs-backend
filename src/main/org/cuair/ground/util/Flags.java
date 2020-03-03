package org.cuair.ground.util;

public class Flags {
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

	// Special username
	public static String MDLC_OPERATOR_USERNAME = "operator";

	// Default username
	public static String DEFAULT_USERNAME = "<NO_USER>";

	// Won't allow two users to have the same ip if set to false
	public static boolean ENABLE_MULTIPLE_USERS_PER_IP = false;

	// Allows clients to create users if true. Otherwise, all will use default user
	public static boolean USERS_ENABLED = true;
}
