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


public class RequestsHelper {
	public static void meep() {
		AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
		ListenableFuture<ResponseEntity<ServerResponse>> entity = asyncRestTemplate.getForEntity("http://localhost:8001/resource", ServerResponse.class);
		entity.addCallback(new ListenableFutureCallback<ResponseEntity<ServerResponse>>() {
            @Override
            public void onFailure(Throwable ex) {
            	System.out.println("FAILURE");
            	System.out.println(ex.getMessage());
            }

            @Override
            public void onSuccess(ResponseEntity<ServerResponse> result) {
            	System.out.println("SUCCESS");
            }
        });

	}

}