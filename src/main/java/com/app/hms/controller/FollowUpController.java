package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.dto.request.FollowUpRequest;
import com.app.hms.dto.response.FollowUpResponse;
import com.app.hms.service.FollowUpService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {
  private final FollowUpService service;

  @PostMapping
  public ApiResponse<FollowUpResponse> create(
      @Valid @RequestBody FollowUpRequest request, Principal principal) {
    return ApiResponse.ok(
        "Follow-up scheduled successfully", service.create(request, principal.getName()));
  }

  @GetMapping("/reminders")
  public ApiResponse<List<FollowUpResponse>> reminders() {
    return ApiResponse.ok(service.reminders());
  }

  @PatchMapping("/{id}/reminded")
  public ApiResponse<FollowUpResponse> markReminded(@PathVariable Long id, Principal principal) {
    return ApiResponse.ok(
        "Patient marked as reminded", service.markReminded(id, principal.getName()));
  }
}
