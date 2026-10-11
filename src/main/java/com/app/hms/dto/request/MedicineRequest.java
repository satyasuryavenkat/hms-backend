package com.app.hms.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MedicineRequest(
    @Size(max = 120) String manufacturerCode,
    @NotBlank @Size(max = 160) String name,
    @Size(max = 160) String genericName,
    @NotBlank @Size(max = 80) String type,
    @Size(max = 160) String manufacturerName,
    @Size(max = 100) String batchNumber,
    @Size(max = 2000) String description,
    String imageData,
    @PositiveOrZero Integer quantity,
    @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
    @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal taxPercent,
    LocalDate expiryDate,
    boolean active) {}
