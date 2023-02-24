package com.welcome.kbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class KbsApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    System.setProperty("server.servlet.context-path", "/kbs/api");
    SpringApplication.run(KbsApplication.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(KbsApplication.class);
  }

}
