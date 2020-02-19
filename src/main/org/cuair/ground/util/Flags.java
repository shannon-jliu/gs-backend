package org.cuair.ground.util;


public class Flags {

    // Image directory
	public static String PLANE_IMAGE_DIR = "images/";

	public static String AUTOPILOT_GROUND_IP = "192.168.0.22";
	public static String AUTOPILOT_GROUND_PORT = "8001";
	public static String AUTOPILOT_COVERAGE = "/ground/api/v3/distributed/geotag";
	public static String AUTOPILOT_GROUND_MDLC_ROIS = "/ground/api/v3/distributed/mdlc";
	public static String AUTOPILOT_GROUND_ADLC_ROIS = "/ground/api/v3/distributed/adlc";

	/* Plane servers IP */
	public static String OBC_IP = "192.168.0.21";

	public static boolean PRINT_CLIENT_LOGS = true;

	// todo 

	public static String CAM_GIM_PORT = "5000";

	public static String AIR_API_PORT = "5001";

	public static String AIRDROP_PORT = "5002";

	/* CGS */

	public static String CAM_GIM_MODE = "/api/mode";

	public static String MISSION_INFO = "/api/missions";

	public static int MISSION_ID = 1;

	public static String POST_TARGET = "/api/odlcs";

	public static int TARGETLOGGER_DELAY = 30000;

	public static String INTEROP_TARGET_DIR = "images/";

    // Flags
    public static boolean AUTH_ENABLED = false;

    // Security
    public static String DEFAULT_USER = "<NO_USER>";

    public static int IMAGE_WIDTH = 5456;

    public static int IMAGE_HEIGHT = 3632;

}
