package com.app.hms.security;

import com.app.hms.entity.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long accessMinutes;
  private final long refreshDays;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-minutes}") long a,
      @Value("${app.jwt.refresh-days}") long r) {
    key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    accessMinutes = a;
    refreshDays = r;
  }

  public String access(AppUser u) {
    return build(u, Duration.ofMinutes(accessMinutes), "access", UUID.randomUUID().toString());
  }

  public String refresh(AppUser u, String id) {
    return build(u, Duration.ofDays(refreshDays), "refresh", id);
  }

  private String build(AppUser u, Duration ttl, String type, String id) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(u.getUsername())
        .id(id)
        .claim("uid", u.getId())
        .claim("role", u.getRole().name())
        .claim("type", type)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  public long expiresIn() {
    return accessMinutes * 60;
  }

  public long refreshDays() {
    return refreshDays;
  }
}
