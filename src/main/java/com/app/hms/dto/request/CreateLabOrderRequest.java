package com.app.hms.dto.request;

import com.app.hms.common.Enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CreateLabOrderRequest(
    @NotNull Long patientId,
    Long referringDoctorId,
    @NotNull LabPriority priority,
    @NotEmpty List<Long> testIds,
    @PositiveOrZero BigDecimal discount,
    @NotNull PaymentMode paymentMode) {}
