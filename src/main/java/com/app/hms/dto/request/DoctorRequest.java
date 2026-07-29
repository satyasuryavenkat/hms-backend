package com.app.hms.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DoctorRequest(
    @NotBlank String doctorCode,
    @NotBlank String name,
    @NotBlank String department,
    String specialization,
    @NotNull @PositiveOrZero BigDecimal consultationFee,
    boolean active) {}
