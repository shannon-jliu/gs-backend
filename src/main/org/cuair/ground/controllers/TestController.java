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

@RestController
public class TestController {

    @RequestMapping(value = "/")
    public String index() {
      return "test";
    }

}
