package com.app.hms.dto.request;

import com.app.hms.common.Enums.SpecimenType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record LabTestRequest(
    @NotBlank String code,
    @NotBlank String name,
    @NotBlank String department,
    @NotNull @PositiveOrZero BigDecimal price,
    @NotNull @Positive Integer turnaroundHours,
    @NotNull SpecimenType specimenType,
    boolean active,
    List<LabParameterRequest> parameters) {}
