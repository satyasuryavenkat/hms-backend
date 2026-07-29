package com.app.hms.controller;

import com.app.hms.common.*;
import com.app.hms.common.Enums.AdmissionStatus;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.service.IpdService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IpdController {
  private final IpdService service;

  @PostMapping("/ipd-admissions")
  public ApiResponse<IpdAdmissionResponse> create(@Valid @RequestBody IpdAdmissionRequest r) {
    return ApiResponse.ok("IPD admission completed", service.create(r));
  }

  @PutMapping("/ipd-admissions/{id}")
  public ApiResponse<IpdAdmissionResponse> update(
      @PathVariable Long id, @Valid @RequestBody IpdAdmissionRequest r) {
    return ApiResponse.ok(service.update(id, r));
  }

  @GetMapping("/ipd-admissions/search")
  public ApiResponse<List<IpdAdmissionResponse>> search(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(required = false) AdmissionStatus status) {
    return ApiResponse.ok(service.search(query, status));
  }

  @GetMapping("/ipd-admissions/{id}")
  public ApiResponse<IpdAdmissionResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.findById(id));
  }

  @GetMapping("/ipd-admissions/{id}/charges")
  public ApiResponse<IpdChargesResponse> charges(@PathVariable Long id) {
    return ApiResponse.ok(service.charges(id));
  }

  @PostMapping("/ipd-admissions/{id}/charges")
  public ApiResponse<IpdChargeResponse> addCharge(
      @PathVariable Long id, @Valid @RequestBody IpdChargeRequest r) {
    return ApiResponse.ok("Charge added", service.addCharge(id, r));
  }

  @PutMapping("/ipd-admissions/{id}/charges/{chargeId}")
  public ApiResponse<IpdChargeResponse> updateCharge(
      @PathVariable Long id, @PathVariable Long chargeId, @Valid @RequestBody IpdChargeRequest r) {
    return ApiResponse.ok(service.updateCharge(id, chargeId, r));
  }

  @DeleteMapping("/ipd-admissions/{id}/charges/{chargeId}")
  public ApiResponse<Void> deleteCharge(@PathVariable Long id, @PathVariable Long chargeId) {
    service.deleteCharge(id, chargeId);
    return ApiResponse.ok("Charge deleted", null);
  }

  @GetMapping("/ipd-charge-catalog")
  public ApiResponse<List<?>> catalog() {
    return ApiResponse.ok(service.catalog());
  }

  @PostMapping("/ipd-admissions/{id}/advances")
  public ApiResponse<IpdAdvanceResponse> advance(
      @PathVariable Long id, @Valid @RequestBody IpdAdvanceRequest r) {
    return ApiResponse.ok("Advance recorded", service.addAdvance(id, r));
  }

  @GetMapping("/ipd-admissions/{id}/advances")
  public ApiResponse<List<IpdAdvanceResponse>> advances(@PathVariable Long id) {
    return ApiResponse.ok(service.advances(id));
  }

  @GetMapping("/ipd-admissions/{id}/final-bill")
  public ApiResponse<FinalBillResponse> bill(@PathVariable Long id) {
    return ApiResponse.ok(service.finalBill(id));
  }

  @PostMapping("/ipd-admissions/{id}/final-bill/settle")
  public ApiResponse<FinalBillResponse> settle(
      @PathVariable Long id, @Valid @RequestBody FinalBillSettlementRequest r) {
    return ApiResponse.ok("Final bill settled", service.settle(id, r));
  }

  @GetMapping(
      value = "/ipd-admissions/{id}/final-bill.pdf",
      produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> billPdf(@PathVariable Long id) {
    return PdfResponseFactory.attachment("ipd-final-bill-" + id + ".pdf", service.finalBillPdf(id));
  }

  @PutMapping("/ipd-admissions/{id}/discharge")
  public ApiResponse<IpdAdmissionResponse> discharge(
      @PathVariable Long id, @Valid @RequestBody DischargeRequest r) {
    return ApiResponse.ok("Discharge draft saved", service.saveDischarge(id, r));
  }

  @PostMapping("/ipd-admissions/{id}/discharge/finalize")
  public ApiResponse<IpdAdmissionResponse> finalizeDischarge(
      @PathVariable Long id, @Valid @RequestBody FinalizeDischargeRequest r) {
    return ApiResponse.ok("Patient discharged successfully", service.finalizeDischarge(id, r));
  }

  @GetMapping("/ipd-admissions/{id}/discharge")
  public ApiResponse<IpdAdmissionResponse> discharge(@PathVariable Long id) {
    return ApiResponse.ok(service.findById(id));
  }

  @GetMapping(
      value = "/ipd-admissions/{id}/discharge.pdf",
      produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> dischargePdf(@PathVariable Long id) {
    return PdfResponseFactory.attachment("discharge-" + id + ".pdf", service.dischargePdf(id));
  }
}
