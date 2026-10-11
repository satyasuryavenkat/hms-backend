package com.app.hms.controller;

import com.app.hms.service.OverviewService;
import com.app.hms.common.ApiResponse;
import com.app.hms.common.BadRequestException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/overview")
@RequiredArgsConstructor
public class OverviewController {
  private final OverviewService overview;

  @GetMapping("/current-beds")
  public ApiResponse<java.util.List<OverviewService.BedPatient>> currentBeds() {
    return ApiResponse.ok(overview.currentBeds());
  }

  @GetMapping
  public ApiResponse<OverviewService.Overview> get(
      @RequestParam(defaultValue = "") String startDate,
      @RequestParam(defaultValue = "") String endDate) {
    try {
      return ApiResponse.ok(overview.get(LocalDate.parse(startDate), LocalDate.parse(endDate)));
    } catch (java.time.format.DateTimeParseException ex) {
      throw new BadRequestException("Start and end dates are required in YYYY-MM-DD format");
    }
  }
}
