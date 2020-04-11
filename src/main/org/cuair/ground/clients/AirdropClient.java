package org.cuair.ground.clients;

import org.cuair.ground.models.plane.settings.AirdropSettings;
import org.cuair.ground.util.Flags;

/**
 * Client for airdrop communications
 */
public class AirdropClient extends SettingsClient<AirdropSettings> { 

  /**
   * Client to communicate with airdrop server
   */
  public AirdropClient() {
    this.serverPort = Flags.AIRDROP_PORT;
    this.setModeRoute = Flags.SET_AIRDROP_SETTINGS_ROUTE;
    this.getModeRoute = Flags.GET_AIRDROP_SETTINGS_ROUTE;
  }
  
}
