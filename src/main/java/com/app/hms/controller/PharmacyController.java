package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.service.PharmacyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {
  private final PharmacyService service;

  @GetMapping("/medicines")
  public ApiResponse<List<MedicineResponse>> medicines(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(required = false) Boolean active) {
    return ApiResponse.ok(service.searchMedicines(query, active));
  }

  @GetMapping("/medicines/{id}")
  public ApiResponse<MedicineResponse> medicine(@PathVariable Long id) {
    return ApiResponse.ok(service.getMedicine(id));
  }

  @PostMapping("/medicines")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<MedicineResponse> create(@Valid @RequestBody MedicineRequest request) {
    return ApiResponse.ok("Medicine created successfully", service.createMedicine(request));
  }

  @PutMapping("/medicines/{id}")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<MedicineResponse> update(
      @PathVariable Long id, @Valid @RequestBody MedicineRequest request) {
    return ApiResponse.ok("Medicine updated successfully", service.updateMedicine(id, request));
  }

  @DeleteMapping("/medicines/{id}")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.deleteMedicine(id);
    return ApiResponse.ok("Medicine deleted successfully", null);
  }

  @PostMapping("/sales")
  public ApiResponse<PharmacySaleResponse> createSale(
      @Valid @RequestBody CreatePharmacySaleRequest request) {
    return ApiResponse.ok("Pharmacy invoice generated successfully", service.createSale(request));
  }

  @GetMapping("/sales/{id}")
  public ApiResponse<PharmacySaleResponse> sale(@PathVariable Long id) {
    return ApiResponse.ok(service.getSale(id));
  }
}
