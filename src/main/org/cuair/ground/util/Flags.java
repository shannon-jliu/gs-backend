package org.cuair.ground.util;

import org.springframework.beans.factory.annotation.Value;

/*
 * To use a flag, add it in src/main/resources/application.properties, and then here, and
 * import this class.
 */
public class Flags {

    @Value("${spring.datasource.password}") public static String CUAIR_PLANE_CAMERA_GIMBAL_KEK;

	@Value("${cuair.plane.camera_gimbal}") public static String CUAIR_PLANE_CAMERA_GIMBAL;

	@Value("${cuair.plane.airdrop}") public static String CUAIR_PLANE_AIRDROP;

	@Value("${cuair.plane.autopilot}") public static String CUAIR_PLANE_AUTOPILOT;

	@Value("${cuair.interop.destination}") public static String CUAIR_INTEROP_DESTINATION;

    @Value("${plane.image.dir}") public static String PLANE_IMAGE_DIR;

    @Value("${plane.image.backup.dir}") public static String PLANE_IMAGE_BACKUP_DIR;

}
