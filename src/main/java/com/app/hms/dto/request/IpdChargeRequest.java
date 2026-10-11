package com.app.hms.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record IpdChargeRequest(
    @NotBlank String category,
    @NotBlank String department,
    @NotBlank String description,
    @NotNull @Positive BigDecimal quantity,
    Long labTestId,
    @PositiveOrZero BigDecimal rate) {}
