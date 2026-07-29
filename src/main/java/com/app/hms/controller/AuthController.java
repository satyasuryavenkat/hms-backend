package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService service;

  @PostMapping("/login")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest r) {
    return ApiResponse.ok("Login successful", service.login(r));
  }

  @PostMapping("/refresh")
  public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest r) {
    return ApiResponse.ok("Token refreshed", service.refresh(r));
  }

  @GetMapping("/me")
  public ApiResponse<UserResponse> me(Authentication a) {
    return ApiResponse.ok(service.currentUser(a.getName()));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest r) {
    service.logout(r);
    return ApiResponse.ok("Logged out successfully", null);
  }
}
