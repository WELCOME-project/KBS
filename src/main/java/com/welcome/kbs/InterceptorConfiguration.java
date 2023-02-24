package com.welcome.kbs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SuppressWarnings("deprecation")
@Component
public class InterceptorConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  BasicAuthenInterceptor productServiceInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(productServiceInterceptor);
  }



}
