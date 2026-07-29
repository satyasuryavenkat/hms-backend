package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.dto.request.PatientRequest;
import com.app.hms.dto.response.*;
import com.app.hms.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {
  private final PatientService service;

  @GetMapping
  public ApiResponse<PatientSearchResponse> findAll(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    return ApiResponse.ok(service.search("", page, size));
  }

  @GetMapping("/search")
  public ApiResponse<PatientSearchResponse> search(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.ok(service.search(query, page, size));
  }

  @PostMapping
  public ApiResponse<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
    return ApiResponse.ok("Patient registered successfully", service.create(request));
  }

  @PutMapping("/{patientId}")
  public ApiResponse<PatientResponse> update(
      @PathVariable Long patientId, @Valid @RequestBody PatientRequest request) {
    return ApiResponse.ok(service.update(patientId, request));
  }

  @GetMapping("/{patientId}")
  public ApiResponse<PatientResponse> findById(@PathVariable Long patientId) {
    return ApiResponse.ok(service.findResponseById(patientId));
  }
}
