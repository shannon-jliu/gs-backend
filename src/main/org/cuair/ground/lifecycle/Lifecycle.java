package org.cuair.ground.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/* Lifecycle component, contains startup and shutdown logic for server. */
@Component
public class Lifecycle {

  @PostConstruct
  public void startUp() {

  }

  @PreDestroy
  public void shutDown() {

  }
}
