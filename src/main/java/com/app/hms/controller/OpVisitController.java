package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.common.PdfResponseFactory;
import com.app.hms.dto.request.OpVisitRequest;
import com.app.hms.dto.response.*;
import com.app.hms.service.OpVisitService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/op-visits")
@RequiredArgsConstructor
public class OpVisitController {
  private final OpVisitService service;

  @PostMapping
  public ApiResponse<OpVisitResponse> create(@Valid @RequestBody OpVisitRequest r) {
    return ApiResponse.ok("OP visit registered successfully", service.create(r));
  }

  @PutMapping("/{visitId}")
  public ApiResponse<OpVisitResponse> update(
      @PathVariable Long visitId, @Valid @RequestBody OpVisitRequest r) {
    return ApiResponse.ok(service.update(visitId, r));
  }

  @GetMapping("/{visitId}")
  public ApiResponse<OpVisitResponse> find(@PathVariable Long visitId) {
    return ApiResponse.ok(service.findById(visitId));
  }

  @GetMapping
  public ApiResponse<OpVisitPageResponse> search(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(required = false) LocalDate date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.search(query, date, page, size));
  }

  @GetMapping("/{visitId}/receipt")
  public ApiResponse<OpVisitResponse> receipt(@PathVariable Long visitId) {
    return ApiResponse.ok(service.findById(visitId));
  }

  @GetMapping(value = "/{visitId}/receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> pdf(@PathVariable Long visitId) {
    return PdfResponseFactory.attachment(
        "op-receipt-" + visitId + ".pdf", service.generateReceiptPdf(visitId));
  }
}
