package org.cuair.ground.util;

public class Flags {
	// Image directory
	public static String PLANE_IMAGE_DIR = "images/";

	// Special username
	public static String MDLC_OPERATOR_USERNAME = "operator";

	// Default username
	public static String DEFAULT_USERNAME = "<NO_USER>";

	// Won't allow two users to have the same ip if set to false
	public static boolean ENABLE_MULTIPLE_USERS_PER_IP = false;

	// Allows clients to create users if true. Otherwise, all will use default user
	public static boolean USERS_ENABLED = true;
}
