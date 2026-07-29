package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.dto.request.LabTestRequest;
import com.app.hms.dto.response.LabTestResponse;
import com.app.hms.service.LabTestService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lab-tests")
@RequiredArgsConstructor
public class LabTestController {
  private final LabTestService service;

  @GetMapping
  public ApiResponse<List<LabTestResponse>> search(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(required = false) String department,
      @RequestParam(required = false) Boolean active) {
    return ApiResponse.ok(service.search(query, department, active));
  }

  @GetMapping("/{testId}")
  public ApiResponse<LabTestResponse> find(@PathVariable Long testId) {
    return ApiResponse.ok(service.findById(testId));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<LabTestResponse> create(@Valid @RequestBody LabTestRequest request) {
    return ApiResponse.ok(service.create(request));
  }

  @PutMapping("/{testId}")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<LabTestResponse> update(
      @PathVariable Long testId, @Valid @RequestBody LabTestRequest request) {
    return ApiResponse.ok("Lab test updated successfully", service.update(testId, request));
  }
}
