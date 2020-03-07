package org.cuair.ground.clients;

/** A factory class that contains all client objects */
public class ClientFactory {

  /** The interop client for communication with the competition server */
  private static InteropClient interopClient = new InteropClient();

  /** The autopilot client for communiation with autopilot ground station */
  private static AutopilotClient autopilotClient = new AutopilotClient();

  /** The airdrop client for communiation with the airdrop server */
  private static AirdropClient airdropClient = new AirdropClient();

  /**
   * Returns the interop client object
   *
   * @return the interop client object
   */
  public static InteropClient getInteropClient() {
    return interopClient;
  }

  /**
   * Returns the autopilot client object
   *
   * @return the autopilot client object
   */
  public static AutopilotClient getAutopilotClient() {
    return autopilotClient;
  }

  /**
   * Returns the airdrop client object
   *
   * @return the airdrop client object
   */
  public static AirdropClient getAirdropClient() {
    return airdropClient;
  }

}
