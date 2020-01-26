package org.cuair.ground.clients;

import java.io.IOException;
import org.springframework.web.client.RestTemplate;
import org.cuair.ground.util.Flags;
import org.springframework.web.reactive.function.client.WebClient;

/** A factory class that contains all client objects */
public class ClientFactory {

  /** The interop client for communication with the competition server */
  private static InteropClient interopClient = new InteropClient();

  /**
   * Returns the interop client object
   *
   * @return the interop client object
   */
  public static InteropClient getInteropClient() {
    return interopClient;
  }

}
