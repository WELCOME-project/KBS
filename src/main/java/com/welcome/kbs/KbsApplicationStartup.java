package com.welcome.kbs;

import com.welcome.services.graphdbGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class KbsApplicationStartup
    implements ApplicationListener<ApplicationReadyEvent> {

  Logger logger = LoggerFactory.getLogger(graphdbGlobal.class);

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {

    logger.info("(INIT) Loading parameters and opening connection with the repository.");
  }
}