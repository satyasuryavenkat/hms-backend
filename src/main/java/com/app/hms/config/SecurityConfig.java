package com.app.hms.config;

import com.app.hms.dao.UserDao;
import com.app.hms.security.JwtFilter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtFilter jwtFilter;

  @Bean
  SecurityFilterChain chain(HttpSecurity h) throws Exception {
    return h.csrf(c -> c.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            s ->
                s.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/actuator/health")
                    .permitAll()
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/asset-manifest.json",
                        "/favicon.ico",
                        "/manifest.json",
                        "/robots.txt",
                        "/logo192.png",
                        "/logo512.png",
                        "/hms-logo.jpeg",
                        "/hms-symbol.png",
                        "/static/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  org.springframework.security.core.userdetails.UserDetailsService userDetails(UserDao users) {
    return name -> {
      var u =
          users
              .findByUsername(name)
              .orElseThrow(
                  () ->
                      new org.springframework.security.core.userdetails.UsernameNotFoundException(
                          name));
      return org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
          .password(u.getPassword())
          .roles(u.getRole().name())
          .disabled(!u.isActive())
          .build();
    };
  }

  @Bean
  AuthenticationManager manager(AuthenticationConfiguration c) throws Exception {
    return c.getAuthenticationManager();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins}") String origins,
      @Value("${app.cors.allowed-methods}") String methods,
      @Value("${app.cors.allowed-headers}") String headers,
      @Value("${app.cors.exposed-headers}") String exposedHeaders,
      @Value("${app.cors.allow-credentials}") boolean allowCredentials,
      @Value("${app.cors.max-age}") long maxAge) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(csv(origins));
    c.setAllowedMethods(csv(methods));
    c.setAllowedHeaders(csv(headers));
    c.setExposedHeaders(csv(exposedHeaders));
    c.setAllowCredentials(allowCredentials);
    c.setMaxAge(maxAge);
    var s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/**", c);
    return s;
  }

  private static List<String> csv(String value) {
    return Arrays.stream(value.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList();
  }
}
