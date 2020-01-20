package org.cuair.ground.util;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.AsyncRestTemplate;
import java.util.concurrent.CompletableFuture;
import org.cuair.ground.util.Flags;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.reactive.function.server.ServerResponse;
import java.net.URI;


public class RequestsHelper {


      // RequestCallback requestCallback() {
      //     return clientHttpRequest -> {
      //         ObjectMapper mapper = new ObjectMapper();
      //         mapper.writeValue(clientHttpRequest.getBody(), updatedInstance);
      //         clientHttpRequest.getHeaders().add(
      //           HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      //         clientHttpRequest.getHeaders().add(
      //           HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedLogPass());
      //     };
      // }


	public static void meep() {
		RestTemplate restTemplate = new RestTemplate();

            URI exampleURI = URI.create("http://localhost:8001/resource");

            AsyncRestTemplate template = new AsyncRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<String>("params", headers);
            var future1 = template.exchange(exampleURI, HttpMethod.GET, requestEntity, String.class).completable();
            var future2 = template.exchange(exampleURI, HttpMethod.GET, requestEntity, String.class).completable();
            var future3 = template.exchange(exampleURI, HttpMethod.GET, requestEntity, String.class).completable();

            CompletableFuture.allOf(future1, future2, future3).thenRun(() -> {
                System.out.println("done HERE");
            });

            //ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8001/resource", String.class);
            //restTemplate.execute("http://localhost:8001/resource", HttpMethod.GET, clientHttpRequest -> null, clientHttpRequest -> null);
            System.out.println("done WITH MEEP");
            //response.

		// ListenableFuture<ResponseEntity<ServerResponse>> entity = asyncRestTemplate.getForEntity("http://localhost:8001/resource", ServerResponse.class);
		// entity.addCallback(new ListenableFutureCallback<ResponseEntity<ServerResponse>>() {
  //           @Override
  //           public void onFailure(Throwable ex) {
  //           	System.out.println("FAILURE");
  //           	System.out.println(ex.getMessage());
  //           }

  //           @Override
  //           public void onSuccess(ResponseEntity<ServerResponse> result) {
  //           	System.out.println("SUCCESS");
  //           }
       // });

	}

}
