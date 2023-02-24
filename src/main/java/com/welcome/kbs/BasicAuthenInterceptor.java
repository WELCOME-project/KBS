package com.welcome.kbs;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RestController
public class BasicAuthenInterceptor implements HandlerInterceptor {

  KbsController kb = new KbsController();

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // String authHeader = request.getHeader("Authorization");

    // System.out.println("Pre Handle method is Calling");
    // request.getSession().setAttribute("isActive", kb.authSend());
    // String url = "http://localhost:8080/kbs/api/validation";

    return true;
  }

}
