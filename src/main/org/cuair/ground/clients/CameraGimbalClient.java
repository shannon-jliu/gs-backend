package org.cuair.ground.clients;

import org.cuair.ground.models.plane.settings.CameraGimbalSettings;
import org.cuair.ground.util.Flags;

/**
 * Client for CGS communications
 */
public class CameraGimbalClient extends SettingsClient<CameraGimbalSettings> {

  /**
   * Client to communicate with camera gimbal server
   */
  public CameraGimbalClient() {
    this.SERVER_PORT = Flags.CAM_GIM_PORT;
    this.SET_MODE_ROUTE = Flags.SET_CAM_GIM_MODE_SETTINGS_ROUTE;
    this.GET_MODE_ROUTE = Flags.GET_CAM_GIM_MODE_SETTINGS_ROUTE;
  }

}
