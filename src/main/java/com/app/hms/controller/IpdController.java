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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IpdController {
  private final IpdService service;

  @PatchMapping("/ipd-admissions/{id}/dates")
  public ApiResponse<IpdAdmissionResponse> updateDates(@PathVariable Long id, @Valid @RequestBody AdmissionDatesRequest request) {
    requireIpd(id);
    return ApiResponse.ok("Admission dates saved", service.updateDates(id, request));
  }

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
    requireIpd(id);
    return ApiResponse.ok(service.charges(id));
  }

  @PostMapping("/ipd-admissions/{id}/charges")
  public ApiResponse<IpdChargeResponse> addCharge(
      @PathVariable Long id, @Valid @RequestBody IpdChargeRequest r) {
    requireIpd(id);
    return ApiResponse.ok("Charge added", service.addCharge(id, r));
  }

  @PutMapping("/ipd-admissions/{id}/charges/{chargeId}")
  public ApiResponse<IpdChargeResponse> updateCharge(
      @PathVariable Long id, @PathVariable Long chargeId, @Valid @RequestBody IpdChargeRequest r) {
    requireIpd(id);
    return ApiResponse.ok(service.updateCharge(id, chargeId, r));
  }

  @DeleteMapping("/ipd-admissions/{id}/charges/{chargeId}")
  public ApiResponse<Void> deleteCharge(@PathVariable Long id, @PathVariable Long chargeId) {
    requireIpd(id);
    service.deleteCharge(id, chargeId);
    return ApiResponse.ok("Charge deleted", null);
  }

  @GetMapping("/ipd-charge-catalog")
  public ApiResponse<List<?>> catalog() {
    return ApiResponse.ok(service.catalog());
  }

  @GetMapping("/ipd-charge-catalog/items")
  public ApiResponse<List<IpdChargeCatalogItemResponse>> catalogItems(
      @RequestParam(required = false) Boolean active) {
    return ApiResponse.ok(service.catalogItems(active));
  }

  @PostMapping("/ipd-charge-catalog/items")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<IpdChargeCatalogItemResponse> createCatalogItem(
      @Valid @RequestBody IpdChargeCatalogRequest request) {
    return ApiResponse.ok("IPD charge configuration created", service.createCatalogItem(request));
  }

  @PutMapping("/ipd-charge-catalog/items/{itemId}")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<IpdChargeCatalogItemResponse> updateCatalogItem(
      @PathVariable Long itemId, @Valid @RequestBody IpdChargeCatalogRequest request) {
    return ApiResponse.ok("IPD charge configuration updated", service.updateCatalogItem(itemId, request));
  }

  @DeleteMapping("/ipd-charge-catalog/items/{itemId}")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ApiResponse<Void> deleteCatalogItem(@PathVariable Long itemId) {
    service.deleteCatalogItem(itemId);
    return ApiResponse.ok("IPD charge configuration deleted", null);
  }

  @PostMapping("/ipd-admissions/{id}/advances")
  public ApiResponse<IpdAdvanceResponse> advance(
      @PathVariable Long id, @Valid @RequestBody IpdAdvanceRequest r) {
    requireIpd(id);
    return ApiResponse.ok("Advance recorded", service.addAdvance(id, r));
  }

  @GetMapping("/ipd-admissions/{id}/advances")
  public ApiResponse<List<IpdAdvanceResponse>> advances(@PathVariable Long id) {
    requireIpd(id);
    return ApiResponse.ok(service.advances(id));
  }

  @GetMapping("/ipd-admissions/{id}/final-bill")
  public ApiResponse<FinalBillResponse> bill(@PathVariable Long id) {
    requireIpd(id);
    return ApiResponse.ok(service.finalBill(id));
  }

  @PostMapping("/ipd-admissions/{id}/final-bill/settle")
  public ApiResponse<FinalBillResponse> settle(
      @PathVariable Long id, @Valid @RequestBody FinalBillSettlementRequest r) {
    requireIpd(id);
    return ApiResponse.ok("Final bill settled", service.settle(id, r));
  }

  @GetMapping(
      value = "/ipd-admissions/{id}/final-bill.pdf",
      produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> billPdf(@PathVariable Long id) {
    requireIpd(id);
    return PdfResponseFactory.attachment("ipd-final-bill-" + id + ".pdf", service.finalBillPdf(id));
  }

  @PutMapping("/ipd-admissions/{id}/discharge")
  public ApiResponse<IpdAdmissionResponse> discharge(
      @PathVariable Long id, @Valid @RequestBody DischargeRequest r) {
    requireIpd(id);
    return ApiResponse.ok("Discharge draft saved", service.saveDischarge(id, r));
  }

  @PostMapping("/ipd-admissions/{id}/discharge/finalize")
  public ApiResponse<IpdAdmissionResponse> finalizeDischarge(
      @PathVariable Long id, @Valid @RequestBody FinalizeDischargeRequest r) {
    requireIpd(id);
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
    requireIpd(id);
    return PdfResponseFactory.attachment("discharge-" + id + ".pdf", service.dischargePdf(id));
  }

  private void requireIpd(Long id) {
    service.findById(id);
  }
}
