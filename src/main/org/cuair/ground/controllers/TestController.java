package org.cuair.ground.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.cuair.ground.util.Flags;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.Collections;
import org.cuair.ground.util.RequestsHelper;

@RestController
public class TestController {

    @RequestMapping(value = "/")
    public String index() {
      RequestsHelper.meep();
      // WebClient client3 = WebClient.builder().baseUrl("http://0.0.0.0:8001").defaultCookie("cookieKey", "cookieValue").defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).defaultUriVariables(Collections.singletonMap("url", "http://localhost:8001")).build();
      // Mono<Flags> fds = client3.method(HttpMethod.POST).uri("/resource").accept().exchange().flatMap(response -> response.bodyToMono(Flags.class));

      return "test";
    }

}
