package com.app.hms.dto.response;

import com.app.hms.common.Enums.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record LabOrderResponse(
    Long id,
    String orderNumber,
    PatientResponse patient,
    DoctorResponse referringDoctor,
    LabPriority priority,
    List<TestItem> tests,
    List<SpecimenLabel> specimens,
    BigDecimal subtotal,
    BigDecimal discount,
    BigDecimal totalPayable,
    BigDecimal paidAmount,
    PaymentMode paymentMode,
    PaymentStatus paymentStatus,
    LabReportStatus reportStatus,
    String remarks,
    OffsetDateTime createdAt) {
  public record TestItem(
      Long testId,
      String code,
      String name,
      String department,
      SpecimenType specimenType,
      BigDecimal amount,
      String status,
      List<ResultItem> results) {}

  public record ResultItem(
      @JsonSerialize(using = ToStringSerializer.class) Long parameterId,
      String name,
      String result,
      String unit,
      String referenceRange,
      String remarks,
      boolean abnormal) {}

  public record SpecimenLabel(
      String barcode,
      SpecimenType specimenType,
      List<String> testCodes,
      OffsetDateTime createdAt) {}
}
