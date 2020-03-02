package org.cuair.ground.clients;

/** A factory class that contains all client objects */
public class ClientFactory {

  /** The interop client for communication with the competition server */
  private static InteropClient interopClient = new InteropClient();

  /** The autopilot client for communiation with autopilot ground station */
  private static AutopilotClient autopilotClient = new AutopilotClient();

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

}
