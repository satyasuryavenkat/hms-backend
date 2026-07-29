package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.dto.request.CreateUserRequest;
import com.app.hms.dto.request.UpdateUserRequest;
import com.app.hms.dto.response.UserResponse;
import com.app.hms.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserController {
  private final UserService service;

  @GetMapping
  public ApiResponse<List<UserResponse>> findAll() {
    return ApiResponse.ok(service.findAll());
  }

  @GetMapping("/{userId}")
  public ApiResponse<UserResponse> findById(@PathVariable Long userId) {
    return ApiResponse.ok(service.findById(userId));
  }

  @PostMapping
  public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    return ApiResponse.ok("User created successfully", service.create(request));
  }

  @PutMapping("/{userId}")
  public ApiResponse<UserResponse> update(
      @PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
    return ApiResponse.ok("User updated successfully", service.update(userId, request));
  }
}
