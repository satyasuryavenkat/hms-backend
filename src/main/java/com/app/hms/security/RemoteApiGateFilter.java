package com.app.hms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class RemoteApiGateFilter extends OncePerRequestFilter {
  private final RemoteApiGate gate;

  public RemoteApiGateFilter(RemoteApiGate gate) {
    this.gate = gate;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return !(path.equals("/api") || path.startsWith("/api/"));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    if (!gate.isEnabled()) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.setHeader("Cache-Control", "no-store");
      response.setHeader("Retry-After", "15");
      response.getWriter().write(
          "{\"code\":\"APPLICATION_DISABLED\",\"message\":\"Unknown Error Please check backend is running and enabled\"}");
      return;
    }
    chain.doFilter(request, response);
  }
}
