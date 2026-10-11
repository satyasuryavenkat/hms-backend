package com.app.hms.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record IpdChargeCatalogRequest(
    @NotBlank @Size(max = 180) String serviceName,
    @NotBlank @Size(max = 120) String category,
    @NotBlank @Size(max = 140) String department,
    @NotNull @DecimalMin("0.0") BigDecimal defaultRate,
    boolean active) {}
