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
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestsHelper;

@Component
public class Lifecycle {

	@PostConstruct
	public void startUp() {
		WebClient airdropClient = WebClient.create("http://" + Flags.CUAIR_PLANE_AIRDROP);
		WebClient autopilotClient = WebClient.create("http://" + Flags.CUAIR_PLANE_AUTOPILOT);
		WebClient cameraGimbalClient = WebClient.create("http://" + Flags.CUAIR_PLANE_CAMERA_GIMBAL);
		WebClient interopClient = WebClient.create("http://" + Flags.CUAIR_INTEROP_DESTINATION);

		//WebClient interopClient = WebClient.create("http://0.0.0.0:8001");

		// WebClient client3 = WebClient.builder().baseUrl("http://localhost:8001").defaultCookie("cookieKey", "cookieValue").defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).defaultUriVariables(Collections.singletonMap("url", "http://localhost:8001")).build();
		// Mono<Flags> fds = client3.method(HttpMethod.POST).uri("/resource").accept().exchange().flatMap(response -> response.bodyToMono(Flags.class));

		// System.out.println("before");
		// Mono<Flags> kek = client3.get().uri("/").retrieve().bodyToMono(Flags.class);
		// System.out.println("after");

		// ClientFactory.initializeAirdropClient(airdropClient);
		// ClientFactory.initializeAutopilotClient(autopilotClient);
		// ClientFactory.initializeCGSClient(cameraGimbalClient);
		RequestsHelper.meep();
		try {
			//ClientFactory.initializeInteropClient(interopClient);

			//findUser();
			//System.out.println("fdfdsafsd");
		} catch (Exception e) {

			System.out.println("exception");
		}

	}

	// @Async
	// public CompletableFuture<Flags> findUser() throws InterruptedException {
	// 	System.out.println("here first");
	//     String url = "http://localhost:8001/resource";
	//     RestTemplate restTemplate = (new RestTemplateBuilder()).build();
	//     Flags results = restTemplate.getForObject(url, Flags.class);
	//     // Artificial delay of 1s for demonstration purposes
	//     Thread.sleep(1000L);
	//     return CompletableFuture.completedFuture(results);
	// }

	@PreDestroy
	public void shutDown() {

	}
}