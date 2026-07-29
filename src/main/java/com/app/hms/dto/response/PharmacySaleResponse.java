package com.app.hms.dto.response;

import com.app.hms.common.Enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record PharmacySaleResponse(
    Long id,
    String invoiceNumber,
    PatientResponse patient,
    String customerName,
    String customerMobile,
    List<Item> items,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal discount,
    BigDecimal totalPayable,
    BigDecimal paidAmount,
    PaymentMode paymentMode,
    PaymentStatus paymentStatus,
    OffsetDateTime createdAt) {
  public record Item(
      Long medicineId,
      String manufacturerCode,
      String medicineName,
      String type,
      String batchNumber,
      LocalDate expiryDate,
      Integer quantity,
      BigDecimal unitPrice,
      BigDecimal taxPercent,
      BigDecimal lineSubtotal,
      BigDecimal taxAmount,
      BigDecimal lineTotal) {}
}
