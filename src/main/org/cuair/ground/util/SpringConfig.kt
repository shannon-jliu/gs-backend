package org.cuair.ground.util

import com.typesafe.config.ConfigFactory
// import org.cuair.ground.models.PlaneSettingsModel

/** Caches all of the Spring configuration variables  */
object SpringConfig {

    private val CONFIG = ConfigFactory.load()

    /** Secret key The secret key is used to secure cryptographic functions.  */
    @JvmField val SPRING_CRYPTO_SECRET = CONFIG.getString("spring.crypto.secret")!!

    // Security
    @JvmField val CUAIR_AUTH_ENABLED = CONFIG.getBoolean("cuair.auth.enabled")
    @JvmField val CUAIR_AUTH_WHITELIST: List<String> = CONFIG.getStringList("cuair.auth.whitelist")
    @JvmField val CUAIR_AUTH_SALT = CONFIG.getString("cuair.auth.salt")!!
    @JvmField val CUAIR_AUTH_USER_PASSWORD_HASH = CONFIG.getString("cuair.auth.user_password_hash")!!
    @JvmField val CUAIR_AUTH_ADMIN_PASSWORD_HASH = CONFIG.getString("cuair.auth.admin_password_hash")!!
    @JvmField val CUAIR_AUTH_CAMERA_PASSWORD_HASH = CONFIG.getString("cuair.auth.camera_password_hash")!!
    @JvmField val CUAIR_AUTH_AUTOPILOT_PASSWORD_HASH = CONFIG.getString("cuair.auth.autopilot_password_hash")!!
    @JvmField val CUAIR_AUTH_VISION_PASSWORD_HASH = CONFIG.getString("cuair.auth.vision_password_hash")!!
    @JvmField val CUAIR_AUTH_JUDGES_PASSWORD_HASH = CONFIG.getString("cuair.auth.judges_password_hash")!!
    @JvmField val CUAIR_AUTH_OVERWRITABLE_USERNAMES: List<String> = CONFIG.getStringList("cuair.auth.overwritable_usernames")
    @JvmField val CUAIR_AUTH_DEFAULT_USERNAME = CONFIG.getString("cuair.auth.default_username")!!

    /** Logging  */
    @JvmField val CUAIR_LOGGING_ENABLED = CONFIG.getBoolean("cuair.logging.enabled")
    /** Autopilot Requests Enable  */
    @JvmField val CUAIR_AUTOPILOT_REQUESTS = CONFIG.getBoolean("cuair.autopilot_requests")
    /** Interop Requests Enable  */
    @JvmField val CUAIR_INTEROP_REQUESTS = CONFIG.getBoolean("cuair.interop_requests")
    /** Airdrop Requests Enable  */
    @JvmField val CUAIR_AIRDROP_REQUESTS = CONFIG.getBoolean("cuair.airdrop_requests") || CONFIG.getBoolean("cuair.all_plane_requests")
    /** Gimbal Requests Enable  */
    @JvmField val CUAIR_GIMBAL_REQUESTS = CONFIG.getBoolean("cuair.gimbal_requests") || CONFIG.getBoolean("cuair.all_plane_requests")
    /** Camera Requests Enable  */
    @JvmField val CUAIR_CAMERA_REQUESTS = CONFIG.getBoolean("cuair.camera_requests") || CONFIG.getBoolean("cuair.all_plane_requests")

    @JvmField val CUAIR_INTEROP_TIMEOUT = CONFIG.getLong("cuair.interop_timeout")

    /**
     * Geotag Alteration on Target Updates (i.e. should be disabled with manual geotagging system!!!)
     */
    @JvmField val CUAIR_GEOTAG_MUTABLE = CONFIG.getBoolean("cuair.geotag.mutable")

    @JvmField val CUAIR_GEOTAG_FOV_HORIZONTAL_RADIANS = CONFIG.getDouble("cuair.geotag.fov_horizontal_radians")
    @JvmField val CUAIR_GEOTAG_FOV_VERTICAL_RADIANS = CONFIG.getDouble("cuair.geotag.fov_vertical_radians")
    @JvmField val CUAIR_GEOTAG_IMAGE_WIDTH = CONFIG.getDouble("cuair.geotag.image_width")
    @JvmField val CUAIR_GEOTAG_IMAGE_HEIGHT = CONFIG.getDouble("cuair.geotag.image_height")
    @JvmField val AVG_MANUAL_WEIGHT = CONFIG.getDouble("cuair.geotag.manualweight")

    /** Plane Image Directory  */
    @JvmField val PLANE_IMAGE_DIR = CONFIG.getString("plane.image.dir")!!

    @JvmField val BACKUP_IMAGE_DIR = CONFIG.getString("plane.image.backup.dir")!!

    // Plane configuration info
    @JvmField val CUAIR_PLANE_GIMBAL = CONFIG.getString("cuair.plane.gimbal")!!
    @JvmField val CUAIR_PLANE_CAMERA = CONFIG.getString("cuair.plane.camera")!!
    @JvmField val CUAIR_PLANE_AIRDROP = CONFIG.getString("cuair.plane.airdrop")!!
    @JvmField val CUAIR_PLANE_AUTOPILOT = CONFIG.getString("cuair.plane.autopilot")!!
    @JvmField val CUAIR_PLANE_AUTOPILOT_GROUND = CONFIG.getString("cuair.plane.autopilot_ground")!!

    @JvmField val CUAIR_INTEROP = CONFIG.getString("cuair.interop")!!

    /** Airdrop state routes  */
    @JvmField val ROUTES_AIRDROP_STATE = CONFIG.getString("routes.airdrop.state")!!
    /** Airdrop settings routes  */
    @JvmField val ROUTES_AIRDROP_SETTINGS = CONFIG.getString("routes.airdrop.settings")!!
    /** Airdrop threshold */
    @JvmField val CUAIR_AIRDROP_THRESHOLD = CONFIG.getDouble("cuair.airdrop.threshold")
    /** Gimbal settings routes  */
    @JvmField val ROUTES_GIMBAL_SETTINGS = CONFIG.getString("routes.gimbal.settings")!!
    /** Gimbal view routes  */
    @JvmField val ROUTES_GIMBAL_VIEW = CONFIG.getString("routes.gimbal.view")!!
    @JvmField val ROUTES_GIMBAL_STATE_DELAY = CONFIG.getLong("routes.gimbal.state_delay")
    /** Camera server routes  */
    @JvmField val ROUTES_CAMERA_SETTINGS = CONFIG.getString("routes.camera.settings")!!
    /** Camera type */
    // @JvmField val CAMERA_TYPE = PlaneSettingsModel.CameraType.valueOf(CONFIG.getString("camera.type"))

    /** Autopilot server routes  */
    @JvmField val ROUTES_AUTOPILOT_STATE_DELAY = CONFIG.getLong("routes.autopilot.state_delay")

    @JvmField val ROUTES_AUTOPILOT_STATE = CONFIG.getString("routes.autopilot.state")!!

    @JvmField val ROUTES_AUTOPILOT_COVERAGE = CONFIG.getString("routes.autopilot.coverage")!!
    @JvmField val ROUTES_AUTOPILOT_AIRDROP = CONFIG.getString("routes.autopilot.airdrop")!!
    @JvmField val ROUTES_AUTOPILOT_GIMBAL = CONFIG.getString("routes.autopilot.gimbal")!!
    @JvmField val ROUTES_AUTOPILOT_ROI_MDLC = CONFIG.getString("routes.autopilot.roi_mdlc")!!
    @JvmField val ROUTES_AUTOPILOT_ROI_ADLC = CONFIG.getString("routes.autopilot.roi_adlc")!!

    // Interop server routes
    @JvmField val ROUTES_INTEROP_LOGIN = CONFIG.getString("routes.interop.login")!!
    @JvmField val ROUTES_INTEROP_TARGETS = CONFIG.getString("routes.interop.targets")!!
    @JvmField val ROUTES_INTEROP_MISSION_DATA = CONFIG.getString("routes.interop.mission_data")!!

    // Interop setting routes
    @JvmField val INTEROP_LOGIN_USERNAME = CONFIG.getString("interop.login.username")!!
    @JvmField val INTEROP_LOGIN_PASSWORD = CONFIG.getString("interop.login.password")!!

    /** Interop Image Directory  */
    @JvmField val INTEROP_TARGET_DIR = CONFIG.getString("interop.target.dir")!!
    /** Target Logger Delay (ms)  */
    @JvmField val TARGETLOGGER_DELAY = CONFIG.getLong("targetlogger.delay")

    /** Vision timeout for skew correction  */
    @JvmField val VISION_TIMEOUT = CONFIG.getLong("vision.timeout")
    /** Number of attempts to contact vision before not contacting them anymore  */
    @JvmField val VISION_ATTEMPTS = CONFIG.getLong("vision.attempts")
    /** Flag to enable skew correction or not  */
    @JvmField val SKEW_ENABLED = CONFIG.getBoolean("skew.enabled")
    /** Amount of time in seconds between iterations to poll for image gimbal data  */
    @JvmField val SKEW_GIMBAL_DELAY = CONFIG.getLong("skew.gimbal.delay")
    /** Amount of attempts of SKEW_GIMBAL_DELAY time before sending image to vision skew correction */
    @JvmField val SKEW_GIMBAL_ATTEMPTS = CONFIG.getLong("skew.gimbal.attempts")

    /** Initial settings sleep between re-requests  */
    @JvmField val INTIAL_SETTINGS_SLEEP = CONFIG.getLong("initial.settings.sleep")

    /** Judge's View config  */
    @JvmField val JV_ENABLED = CONFIG.getBoolean("jv.enabled")

    @JvmField val JV_IP = CONFIG.getString("jv.ip")!!
    @JvmField val JV_UPD_ALPHA = CONFIG.getString("jv.alpha")!!
    @JvmField val JV_UPD_AIRDP = CONFIG.getString("jv.airdp")!!

    /** Logging config  */
    @JvmField val CUAIR_LOG_FILEPATH = CONFIG.getString("cuair.logging.filepath")!!

    /** Logging filename */
    @JvmField val CUAIR_LOG_FILENAME = CONFIG.getString("cuair.logging.filename")!!

    /** HTTP protocol for client requests */
    @JvmField val CUAIR_HTTP_PROTOCOL = CONFIG.getString("cuair.http.protocol")!!
}
