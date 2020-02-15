package org.cuair.ground.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.cuair.ground.clients.ClientFactory;
import org.cuair.ground.util.Flags;

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
			logger.error("Startup exception: " + e.getMessage());
		}
	}

	@PreDestroy
	public void shutDown() {
		
	}
}
