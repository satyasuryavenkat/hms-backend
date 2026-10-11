package com.app.hms.controller;

import com.app.hms.common.ApiResponse;
import com.app.hms.dto.response.PageResponse;
import com.app.hms.service.BillingRecordsService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BillingRecordsController {
  private final BillingRecordsService service;

  @GetMapping({"/op-visits/history", "/lab-orders/history", "/ipd-admissions/history", "/daycare-admissions/history", "/pharmacy/sales/history"})
  public ApiResponse<PageResponse<BillingRecordsService.Detail>> history(jakarta.servlet.http.HttpServletRequest request,
      @RequestParam(defaultValue="") String query, @RequestParam(required=false) LocalDate startDate,
      @RequestParam(required=false) LocalDate endDate, @RequestParam(defaultValue="0") int page,
      @RequestParam(defaultValue="20") int size) {
    String key = switch(request.getServletPath()) { case "/api/v1/op-visits/history" -> "op"; case "/api/v1/lab-orders/history" -> "lab"; case "/api/v1/ipd-admissions/history" -> "ipd"; case "/api/v1/daycare-admissions/history" -> "daycare"; default -> "pharmacy"; };
    return ApiResponse.ok(service.records(key, query, startDate, endDate, page, size, false));
  }

  @GetMapping("/overview/{section:op|ipd|daycare|lab}/records")
  public ApiResponse<PageResponse<BillingRecordsService.Detail>> overview(@PathVariable String section,
      @RequestParam(defaultValue="") String query, @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate, @RequestParam(defaultValue="0") int page,
      @RequestParam(defaultValue="20") int size) {
    return ApiResponse.ok(service.records(section, query, startDate, endDate, page, size, true));
  }
}
