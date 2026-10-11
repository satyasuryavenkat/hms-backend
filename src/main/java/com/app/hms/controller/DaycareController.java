package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.common.Enums.AdmissionStatus;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.service.IpdService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/daycare-admissions")
@RequiredArgsConstructor
public class DaycareController {
  private final IpdService service;

  @PatchMapping("/{id}/dates")
  public ApiResponse<IpdAdmissionResponse> updateDates(@PathVariable Long id, @Valid @RequestBody AdmissionDatesRequest request) {
    requireDaycare(id);
    return ApiResponse.ok("Admission dates saved", service.updateDates(id, request));
  }

  @PostMapping
  public ApiResponse<IpdAdmissionResponse> create(@Valid @RequestBody IpdAdmissionRequest request) {
    return ApiResponse.ok("Daycare admission completed", service.createDaycare(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<IpdAdmissionResponse> update(
      @PathVariable Long id, @Valid @RequestBody IpdAdmissionRequest request) {
    return ApiResponse.ok("Daycare admission updated", service.updateDaycare(id, request));
  }

  @GetMapping("/search")
  public ApiResponse<List<IpdAdmissionResponse>> search(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(required = false) AdmissionStatus status) {
    return ApiResponse.ok(service.searchDaycare(query, status));
  }

  @GetMapping("/{id}")
  public ApiResponse<IpdAdmissionResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.findDaycareById(id));
  }

  @GetMapping("/charge-catalog")
  public ApiResponse<List<?>> catalog() {
    return ApiResponse.ok(service.catalog());
  }

  @GetMapping("/{id}/charges")
  public ApiResponse<IpdChargesResponse> charges(@PathVariable Long id) {
    requireDaycare(id);
    return ApiResponse.ok(service.charges(id));
  }

  @PostMapping("/{id}/charges")
  public ApiResponse<IpdChargeResponse> addCharge(
      @PathVariable Long id, @Valid @RequestBody IpdChargeRequest request) {
    requireDaycare(id);
    return ApiResponse.ok("Daycare charge added", service.addCharge(id, request));
  }

  @PutMapping("/{id}/charges/{chargeId}")
  public ApiResponse<IpdChargeResponse> updateCharge(
      @PathVariable Long id,
      @PathVariable Long chargeId,
      @Valid @RequestBody IpdChargeRequest request) {
    requireDaycare(id);
    return ApiResponse.ok("Daycare charge updated", service.updateCharge(id, chargeId, request));
  }

  @DeleteMapping("/{id}/charges/{chargeId}")
  public ApiResponse<Void> deleteCharge(@PathVariable Long id, @PathVariable Long chargeId) {
    requireDaycare(id);
    service.deleteCharge(id, chargeId);
    return ApiResponse.ok("Daycare charge deleted", null);
  }

  @PostMapping("/{id}/advances")
  public ApiResponse<IpdAdvanceResponse> addAdvance(
      @PathVariable Long id, @Valid @RequestBody IpdAdvanceRequest request) {
    requireDaycare(id);
    return ApiResponse.ok("Daycare advance recorded", service.addAdvance(id, request));
  }

  @GetMapping("/{id}/advances")
  public ApiResponse<List<IpdAdvanceResponse>> advances(@PathVariable Long id) {
    requireDaycare(id);
    return ApiResponse.ok(service.advances(id));
  }

  @GetMapping("/{id}/final-bill")
  public ApiResponse<FinalBillResponse> finalBill(@PathVariable Long id) {
    requireDaycare(id);
    return ApiResponse.ok(service.finalBill(id));
  }

  @PostMapping("/{id}/final-bill/settle")
  public ApiResponse<FinalBillResponse> settle(
      @PathVariable Long id, @Valid @RequestBody FinalBillSettlementRequest request) {
    requireDaycare(id);
    return ApiResponse.ok("Daycare final bill settled", service.settle(id, request));
  }

  @PutMapping("/{id}/discharge")
  public ApiResponse<IpdAdmissionResponse> saveDischarge(
      @PathVariable Long id, @Valid @RequestBody DischargeRequest request) {
    requireDaycare(id);
    return ApiResponse.ok("Daycare completion draft saved", service.saveDischarge(id, request));
  }

  @PostMapping("/{id}/discharge/finalize")
  public ApiResponse<IpdAdmissionResponse> finalizeDischarge(
      @PathVariable Long id, @Valid @RequestBody FinalizeDischargeRequest request) {
    requireDaycare(id);
    return ApiResponse.ok(
        "Daycare visit completed successfully", service.finalizeDischarge(id, request));
  }

  @GetMapping("/{id}/discharge")
  public ApiResponse<IpdAdmissionResponse> discharge(@PathVariable Long id) {
    return ApiResponse.ok(service.findDaycareById(id));
  }

  private void requireDaycare(Long id) {
    service.findDaycareById(id);
  }
}
