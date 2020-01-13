package org.cuair.ground.util;

import org.springframework.beans.factory.annotation.Value;

public class Flags {
	
	@Value("${cuair.plane.camera_gimbal}") public static String CUAIR_PLANE_CAMERA_GIMBAL;

	@Value("${cuair.plane.airdrop}") public static String CUAIR_PLANE_AIRDROP;

	@Value("${cuair.plane.autopilot}") public static String CUAIR_PLANE_AUTOPILOT;

	@Value("${cuair.interop.destination}") public static String CUAIR_INTEROP_DESTINATION;
}
