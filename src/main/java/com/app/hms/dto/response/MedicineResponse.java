package com.app.hms.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record MedicineResponse(
    Long id,
    String manufacturerCode,
    String name,
    String genericName,
    String type,
    String manufacturerName,
    String batchNumber,
    String description,
    String imageData,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal taxPercent,
    LocalDate expiryDate,
    boolean active,
    boolean expired,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
