package com.app.hms.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record AdmissionDatesRequest(@NotNull LocalDateTime admissionDateTime, OffsetDateTime dischargeDateTime) {}
