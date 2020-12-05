package org.cuair.ground.util;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class RequestUtil {

  private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);

  private static boolean printClientLogs = Flags.PRINT_CLIENT_LOGS;

  /**
   * Default on success handler for callbacks
   *
   * @param uri uri of the corresponding request
   */
  public static void defaultOnSuccess(URI uri) {
    printIfFlag("Request: " + uri.toString() + " succeeded!");
  }

  /**
   * Default on failure handler for callbacks
   *
   * @param uri uri of the corresponding request
   * @param ex  exception thrown from the corresponding request
   */
  public static void defaultOnFailure(URI uri, Throwable ex) {
    logger.error("Request: " + uri.toString() + " failed: " + ex.getMessage());
  }

  public interface SuccessCallback<T> {
    void callbackFunction(ResponseEntity<T> result);
  }

  public interface FailureCallback {
    void callbackFunction(Throwable ex);
  }

  /**
   * Logs message if client print flag is true
   *
   * @param message message to print
   */
  public static void printIfFlag(String message) {
    if (printClientLogs) {
      logger.info(message);
    }
  }

  /**
   * Performs default success and callback functions when future completes - i.e. prints
   * success or failure.
   *
   * @param uri    request url
   * @param future the actual request future
   */
  public static void futureCallback(URI uri, ListenableFuture<ResponseEntity<String>> future) {
    future.addCallback(result -> defaultOnSuccess(uri), ex -> defaultOnFailure(uri, ex));
  }

  /**
   * Performs *specified* success and callback functions when future completes, for
   * customization.
   *
   * @param uri             request url
   * @param future          the actual request future
   * @param successCallback custom success callback
   * @param failureCallback custom failure callback
   */
  public static void futureCallback(
      URI uri,
      ListenableFuture<ResponseEntity<String>> future,
      SuccessCallback successCallback,
      FailureCallback failureCallback) {
    future.addCallback(result -> successCallback.callbackFunction(result),
        ex -> failureCallback.callbackFunction(ex));
  }

  /**
   * Performs specified success callback and default failure callback when future completes.
   *
   * @param uri             request url
   * @param future          the actual request future
   * @param successCallback custom success callback
   */
  public static void futureCallback(
      URI uri, ListenableFuture<ResponseEntity<String>> future, SuccessCallback successCallback) {
    future.addCallback(result -> successCallback.callbackFunction(result),
        ex -> defaultOnFailure(uri, ex));
  }

  /**
   * Performs default success callback and specified failure callback when future completes.
   *
   * @param uri             request url
   * @param future          the actual request future
   * @param failureCallback custom failure callback
   */
  public static void futureCallback(
      URI uri, ListenableFuture<ResponseEntity<String>> future, FailureCallback failureCallback) {
    future.addCallback(result -> defaultOnSuccess(uri), ex -> failureCallback.callbackFunction(ex));
  }

  /**
   * Helper method to provide default HTTP headers.
   */
  public static HttpHeaders getDefaultHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  /**
   * Helper method to provide default HTTP headers with the cookie attached to request headers.
   * Used to send requests to interop.
   *
   * @param cookieValue cookie recevied by interop on authentication
   */
  public static HttpHeaders getDefaultCookieHeaders(String cookieValue) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", String.format("sessionid=%s", cookieValue));
    return headers;
  }
}
