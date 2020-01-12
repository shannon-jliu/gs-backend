package org.cuair.ground.clients;

import java.io.IOException;
import org.springframework.web.client.RestTemplate;

/** A factory class that contains all client objects */
public class ClientFactory {

  /** The interop client for communication with the competition server */
  private static InteropClient interopClient;

  /** State variable to indicate whether or not the interop client has been started */
  private static boolean interopClientStarted;

  /** The web service client to perform requests */
  //private static WSClient ws;

  /**
   * Initialize the WSClient used by all ground server clients
   *
   * @param ws the WSClient object
   */
  // public static void initializeClient(WSClient ws) {
  //   ClientFactory.ws = ws;
  // }

  /**
   * Returns the interop client object
   *
   * @return the interop client object
   */
  public static InteropClient getInteropClient() {
    if (interopClient == null) {
      //interopClient = new InteropClient(ws);
    }

    if (/*PlayConfig.CUAIR_INTEROP_REQUESTS && */!interopClientStarted) {
      interopClient.start();
      interopClientStarted = true;
    }
    return interopClient;
  }

  /** Stops all clients that have been started. */
  public static void stopAllClients() {
    if (interopClientStarted) {
      interopClient.stop();
    }
    /*try {
      ;//ws.close();
    } catch (IOException e) {
      e.printStackTrace();
    }*/
  }
}
