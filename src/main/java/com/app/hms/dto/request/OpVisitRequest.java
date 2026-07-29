package com.app.hms.dto.request;

import com.app.hms.common.Enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record OpVisitRequest(
    Long patientId,
    @Valid PatientRequest patient,
    @NotNull Long doctorId,
    @NotNull VisitType visitType,
    @NotNull LocalDate appointmentDate,
    @NotNull LocalTime appointmentTime,
    String symptoms,
    @NotNull @PositiveOrZero BigDecimal consultationFee,
    @NotNull @PositiveOrZero BigDecimal discount,
    @NotNull PaymentMode paymentMode) {}
