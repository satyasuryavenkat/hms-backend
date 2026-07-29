package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.dto.request.DoctorRequest;
import com.app.hms.dto.response.DoctorResponse;
import com.app.hms.service.DoctorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {
  private final DoctorService service;

  @GetMapping
  public ApiResponse<List<DoctorResponse>> findAll(
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false) String department) {
    return ApiResponse.ok(service.findAll(active, department));
  }

  @GetMapping("/{doctorId}")
  public ApiResponse<DoctorResponse> findById(@PathVariable Long doctorId) {
    return ApiResponse.ok(service.findResponseById(doctorId));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<DoctorResponse> create(@Valid @RequestBody DoctorRequest request) {
    return ApiResponse.ok("Doctor created successfully", service.create(request));
  }

  @PutMapping("/{doctorId}")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<DoctorResponse> update(
      @PathVariable Long doctorId, @Valid @RequestBody DoctorRequest request) {
    return ApiResponse.ok("Doctor updated successfully", service.update(doctorId, request));
  }
}
