package com.app.hms.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record IpdChargeRequest(
    @NotBlank String category,
    @NotBlank String department,
    String serviceCode,
    @NotBlank String description,
    @NotNull @Positive BigDecimal quantity,
    @NotNull @PositiveOrZero BigDecimal rate) {}
