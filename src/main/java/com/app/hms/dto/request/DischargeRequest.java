package com.app.hms.dto.request;

import com.app.hms.common.Enums.DischargeType;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;

public record DischargeRequest(
    @NotNull OffsetDateTime dischargeDateTime,
    @NotNull DischargeType dischargeType,
    @NotBlank String finalDiagnosis,
    @NotBlank String clinicalSummary,
    List<MedicationRequest> medications,
    String advice,
    LocalDate followUpDate,
    Long followUpDoctorId) {}
