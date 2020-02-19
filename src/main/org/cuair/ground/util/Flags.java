package org.cuair.ground.util;

public class Flags {

    // Image directory
	public static String PLANE_IMAGE_DIR = "images/";

    // Constants
    private static double FOV_HORIZONTAL_RADIANS = 0.7328394987;
    private static double FOV_VERTICAL_RADIANS = 0.560476881;
    private static double IMAGE_WIDTH = 4912.0;
    private static double IMAGE_HEIGHT = 3684.0;

    // Flags
    public static boolean AUTH_ENABLED = false;
    public static boolean CUAIR_INTEROP_REQUESTS = false;
    public static boolean CUAIR_GEOTAG_MUTABLE = false;

    // Security
    public static String DEFAULT_USER = "<NO_USER>";

}
