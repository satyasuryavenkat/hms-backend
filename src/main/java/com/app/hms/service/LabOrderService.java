package com.app.hms.service;

import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import java.time.LocalDate;

public interface LabOrderService {
  LabOrderResponse create(CreateLabOrderRequest request);

  PageResponse<LabOrderResponse> search(
      String query, LabReportStatus status, LocalDate date, int page, int size);

  LabOrderResponse findById(Long id);

  LabOrderResponse cancel(Long id, CancelLabOrderRequest request);

  LabOrderResponse saveReport(Long id, SaveLabReportRequest request);

  LabOrderResponse publish(Long id, PublishLabReportRequest request);

  byte[] receiptPdf(Long id);

  byte[] reportPdf(Long id);
}
