package org.cuair.ground.clients;

import java.io.IOException;
import org.springframework.web.client.RestTemplate;
import org.cuair.ground.util.Flags;
import org.springframework.web.reactive.function.client.WebClient;

/** A factory class that contains all client objects */
public class ClientFactory {

  /** The interop client for communication with the competition server */
  private static InteropClient interopClient;

  /** State variable to indicate whether or not the interop client has been started */
  private static boolean interopClientStarted;

  /** The web service client to perform requests */
  private static WebClient airdropWebClient;

  /** The web service client to perform requests */
  private static WebClient autopilotWebClient;

  private static WebClient cgsWebClient;

  private static WebClient interopWebClient;

  /**
   * Initialize the RestTemplate used by all ground server clients
   *
   * @param rt the RestTemplate object
   */
  public static void initializeAirdropClient(WebClient airdropClient) {
    ClientFactory.airdropWebClient = airdropClient;
  }

  public static void initializeAutopilotClient(WebClient autopilotClient) {
    ClientFactory.autopilotWebClient = autopilotClient;
  }

  public static void initializeCGSClient(WebClient cgsClient) {
    ClientFactory.cgsWebClient = cgsClient;
  }

  public static void initializeInteropClient(WebClient interopClient) {
    ClientFactory.interopWebClient = interopClient;
    ClientFactory.interopClient = new InteropClient(interopClient);
    try {
      ClientFactory.interopClient.findUser();

    } catch (Exception e) {
      ;//
    }
    System.out.println("DONE");

  }

  /**
   * Returns the interop client object
   *
   * @return the interop client object
   */
  public static InteropClient getInteropClient() {
    if (!interopClientStarted) {
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
      ;//rt.close();
    } catch (IOException e) {
      e.printStackTrace();
    }*/
  }
}
