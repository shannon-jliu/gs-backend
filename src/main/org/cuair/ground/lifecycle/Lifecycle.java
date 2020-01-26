package org.cuair.ground.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.cuair.ground.clients.ClientFactory;
import org.cuair.ground.util.Flags;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.Collections;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Lifecycle {

	// logger
  	private static final Logger logger = LoggerFactory.getLogger(Lifecycle.class);

	@PostConstruct
	public void startUp() {
		try {
			ClientFactory.getInteropClient().attemptLogin();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
		}
	}

	@PreDestroy
	public void shutDown() {

	}
}