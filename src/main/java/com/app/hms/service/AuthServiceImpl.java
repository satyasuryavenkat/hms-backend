package com.app.hms.service;

import com.app.hms.common.BadRequestException;
import com.app.hms.dao.*;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.entity.*;
import com.app.hms.security.JwtService;
import io.jsonwebtoken.Claims;
import java.time.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
  private final AuthenticationManager authenticationManager;
  private final UserDao users;
  private final RefreshTokenDao tokens;
  private final JwtService jwt;

  @Override
  @Transactional
  public AuthResponse login(LoginRequest r) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(r.username(), r.password()));
    AppUser u = user(r.username());
    if (u.getRole() != r.role()) throw new BadRequestException("Invalid role");
    return issue(u);
  }

  @Override
  @Transactional
  public AuthResponse refresh(RefreshTokenRequest r) {
    Claims c = parse(r.refreshToken());
    if (!"refresh".equals(c.get("type"))) throw new BadRequestException("Invalid refresh token");
    RefreshToken old =
        tokens
            .findById(c.getId())
            .filter(t -> !t.isRevoked() && t.getExpiresAt().isAfter(Instant.now()))
            .orElseThrow(() -> new BadRequestException("Refresh token expired or revoked"));
    old.setRevoked(true);
    tokens.save(old);
    return issue(
        users
            .findById(old.getUserId())
            .orElseThrow(() -> new BadRequestException("User no longer exists")));
  }

  @Override
  public UserResponse currentUser(String username) {
    return map(user(username));
  }

  @Override
  @Transactional
  public void logout(RefreshTokenRequest r) {
    try {
      Claims c = jwt.parse(r.refreshToken());
      tokens
          .findById(c.getId())
          .ifPresent(
              t -> {
                t.setRevoked(true);
                tokens.save(t);
              });
    } catch (Exception ignored) {
    }
  }

  private AuthResponse issue(AppUser u) {
    String id = UUID.randomUUID().toString();
    String rt = jwt.refresh(u, id);
    RefreshToken t = new RefreshToken();
    t.setTokenId(id);
    t.setUserId(u.getId());
    t.setExpiresAt(Instant.now().plus(Duration.ofDays(jwt.refreshDays())));
    tokens.save(t);
    return new AuthResponse(jwt.access(u), rt, "Bearer", jwt.expiresIn(), map(u));
  }

  private UserResponse map(AppUser u) {
    return new UserResponse(
        u.getId(),
        u.getUsername(),
        u.getDisplayName(),
        u.getRole(),
        u.isActive(),
        u.getDisplayName().substring(0, 1).toUpperCase(),
        u.getPermissions());
  }

  private AppUser user(String name) {
    return users.findByUsername(name).orElseThrow(() -> new BadRequestException("User not found"));
  }

  private Claims parse(String t) {
    try {
      return jwt.parse(t);
    } catch (Exception e) {
      throw new BadRequestException("Invalid refresh token");
    }
  }
}
