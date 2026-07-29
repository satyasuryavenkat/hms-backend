package com.app.hms.dto.response;

import com.app.hms.common.Enums.*;
import java.math.BigDecimal;
import java.time.*;

public record OpVisitResponse(
    Long visitId,
    String opNumber,
    String receiptNumber,
    PatientResponse patient,
    DoctorResponse doctor,
    VisitType visitType,
    LocalDate appointmentDate,
    LocalTime appointmentTime,
    String symptoms,
    BigDecimal consultationFee,
    BigDecimal discount,
    BigDecimal totalPayable,
    BigDecimal paidAmount,
    PaymentMode paymentMode,
    PaymentStatus paymentStatus,
    OffsetDateTime createdAt) {}
