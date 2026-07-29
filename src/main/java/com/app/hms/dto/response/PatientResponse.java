package com.app.hms.dto.response;

import com.app.hms.common.Enums.Gender;
import java.time.*;

public record PatientResponse(
    Long id,
    String uhid,
    String name,
    String mobile,
    LocalDate dateOfBirth,
    int age,
    Gender gender,
    String address,
    OffsetDateTime createdAt) {}
