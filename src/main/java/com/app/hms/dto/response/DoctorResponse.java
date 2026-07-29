package com.app.hms.dto.response;

import java.math.BigDecimal;

public record DoctorResponse(
    Long id,
    String doctorCode,
    String name,
    String department,
    String specialization,
    BigDecimal consultationFee,
    boolean active) {}
