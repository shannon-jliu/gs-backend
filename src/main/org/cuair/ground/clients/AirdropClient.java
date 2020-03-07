package org.cuair.ground.clients;

import org.cuair.ground.models.plane.settings.*;
import org.cuair.ground.util.Flags;

/*
 * Client for airdrop communications
 */
public class AirdropClient extends SettingsClient<AirdropSettings>{ 

  public AirdropClient() {
    this.SERVER_PORT = Flags.AIRDROP_PORT;
    this.SET_MODE_ROUTE = Flags.SET_AIRDROP_SETTINGS_ROUTE;
    this.GET_MODE_ROUTE = Flags.GET_AIRDROP_SETTINGS_ROUTE;
  }
  
}

