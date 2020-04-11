package org.cuair.ground.util;

import java.net.URI;
import org.json.*;
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

  public interface SuccessCallback<T> {
    void callbackFunction(ResponseEntity<T> result);
  }

  public interface FailureCallback {
    void callbackFunction(Throwable ex);
  }

  public static void printIfFlag(String message) {
    if (printClientLogs) {
      logger.info(message);
    }
  }

  public static void futureCallback(URI uri, ListenableFuture<ResponseEntity<String>> future) {
    future.addCallback(
        new ListenableFutureCallback<ResponseEntity<String>>() {

          @Override
          public void onSuccess(ResponseEntity<String> result) {
            printIfFlag("Request: " + uri.toString() + " succeeded!");
            System.out.println(result.getBody());
          }

          @Override
          public void onFailure(Throwable ex) {
            logger.error("Request: " + uri.toString() + " failed: " + ex.getMessage());
          }
        });
  }

  public static void futureCallback(
      URI uri,
      ListenableFuture<ResponseEntity<String>> future,
      SuccessCallback successCallback,
      FailureCallback failureCallback) {
    future.addCallback(
        new ListenableFutureCallback<ResponseEntity<String>>() {

          @Override
          public void onSuccess(ResponseEntity<String> result) {
            successCallback.callbackFunction(result);
          }

          @Override
          public void onFailure(Throwable ex) {
            failureCallback.callbackFunction(ex);
          }
        });
  }

  public static void futureCallback(
      URI uri, ListenableFuture<ResponseEntity<String>> future, SuccessCallback successCallback) {
    future.addCallback(
        new ListenableFutureCallback<ResponseEntity<String>>() {

          @Override
          public void onSuccess(ResponseEntity<String> result) {
            successCallback.callbackFunction(result);
          }

          @Override
          public void onFailure(Throwable ex) {
            logger.error("Request: " + uri.toString() + " failed: " + ex.getMessage());
          }
        });
  }

  public static void futureCallback(
      URI uri, ListenableFuture<ResponseEntity<String>> future, FailureCallback failureCallback) {
    future.addCallback(
        new ListenableFutureCallback<ResponseEntity<String>>() {

          @Override
          public void onSuccess(ResponseEntity<String> result) {
            printIfFlag("Request: " + uri.toString() + " succeeded!");
          }

          @Override
          public void onFailure(Throwable ex) {
            failureCallback.callbackFunction(ex);
          }
        });
  }

  public static HttpHeaders getDefaultHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  public static HttpHeaders getDefaultCookieHeaders(String cookieValue) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", String.format("sessionid=%s", cookieValue));
    return headers;
  }
}