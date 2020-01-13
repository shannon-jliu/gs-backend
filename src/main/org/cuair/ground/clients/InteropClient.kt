package org.cuair.ground.clients

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

open class InteropClient(private val wc: WebClient) : Client() {

  // @Async
  // open fun findUser(user: String) {
  //   String url = "http://localhost:8001/resource";
  //   RestTemplate restTemplate = (RestTemplateBuilder()).build();
  //   Flags results = restTemplate.getForObject(url, Flags.class);
  //   // Artificial delay of 1s for demonstration purposes
  //   Thread.sleep(1000L);
  //   return CompletableFuture.completedFuture(results);

  // }

  @Async
  @Throws(InterruptedException::class)
  fun findUser() {
    val asycTemp = AsyncRestTemplate();
    val url = "http://localhost:8001/resource"
    val method = HttpMethod.GET;
    val responseType = String::class.java;
    val headers = HttpHeaders();
    val requestEntity = HttpEntity<String>("params", headers);
    val future = asycTemp.exchange(url, method, requestEntity, responseType);
    // future.addCallback(ListenableFutureCallback<ResponseEntity<ServerResponse>>() {
    
    //   override fun onFailure(ex: Throwable) {

    //   }

      
    //   override fun onSuccess(result: ResponseEntity<ServerResponse>) {

    //   }
    // })
    //return 0;
    //val restTemplate = RestTemplate();//RestTemplate(HttpComponentsClientHttpRequestFactory());//(RestTemplateBuilder()).build()
   // val response = restTemplate.getForObject(url, String::class.java)


    //val response = restTemplate.getForEntity(url, String::class.java);


    // Artificial delay of 1s for demonstration purposes
    //return 9;
    //return CompletableFuture.completedFuture(response)


    //val asycTemp = AsyncRestTemplate();
    //val url ="http:localhost:8001/resource";

  }

  override fun run() {
    
  }

}
