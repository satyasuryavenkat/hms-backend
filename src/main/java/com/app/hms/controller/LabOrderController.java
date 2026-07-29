package com.app.hms.controller;

import com.app.hms.common.*;
import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.service.LabOrderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LabOrderController {
  private final LabOrderService service;

  @PostMapping("/lab-orders")
  public ApiResponse<LabOrderResponse> create(@Valid @RequestBody CreateLabOrderRequest r) {
    return ApiResponse.ok("Lab order generated successfully", service.create(r));
  }

  @GetMapping("/lab-orders")
  public ApiResponse<PageResponse<LabOrderResponse>> search(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(required = false) LabReportStatus status,
      @RequestParam(required = false) LocalDate date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.search(query, status, date, page, size));
  }

  @GetMapping("/lab-orders/{id}")
  public ApiResponse<LabOrderResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.findById(id));
  }

  @PatchMapping("/lab-orders/{id}/cancel")
  public ApiResponse<LabOrderResponse> cancel(
      @PathVariable Long id, @Valid @RequestBody CancelLabOrderRequest r) {
    return ApiResponse.ok("Lab order cancelled", service.cancel(id, r));
  }

  @GetMapping("/lab-orders/{id}/receipt")
  public ApiResponse<LabOrderResponse> receipt(@PathVariable Long id) {
    return ApiResponse.ok(service.findById(id));
  }

  @GetMapping(value = "/lab-orders/{id}/receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> receiptPdf(@PathVariable Long id) {
    return PdfResponseFactory.attachment("lab-receipt-" + id + ".pdf", service.receiptPdf(id));
  }

  @GetMapping("/lab-reports/pending")
  public ApiResponse<PageResponse<LabOrderResponse>> pending(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.search(query, LabReportStatus.PENDING, null, page, size));
  }

  @GetMapping("/lab-orders/{id}/report")
  public ApiResponse<LabOrderResponse> report(@PathVariable Long id) {
    return ApiResponse.ok(service.findById(id));
  }

  @PutMapping("/lab-orders/{id}/report")
  public ApiResponse<LabOrderResponse> save(
      @PathVariable Long id, @Valid @RequestBody SaveLabReportRequest r) {
    return ApiResponse.ok("Report draft saved", service.saveReport(id, r));
  }

  @PostMapping("/lab-orders/{id}/report/publish")
  public ApiResponse<LabOrderResponse> publish(
      @PathVariable Long id, @Valid @RequestBody PublishLabReportRequest r) {
    return ApiResponse.ok("Lab report published", service.publish(id, r));
  }

  @GetMapping(value = "/lab-orders/{id}/report.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> reportPdf(@PathVariable Long id) {
    return PdfResponseFactory.attachment("lab-report-" + id + ".pdf", service.reportPdf(id));
  }
}
