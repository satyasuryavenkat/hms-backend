package com.app.hms.dto.request;

import com.app.hms.common.Enums.AdmissionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;

public record IpdAdmissionRequest(
    @NotNull Long patientId,
    @NotNull AdmissionType admissionType,
    @NotNull LocalDate admissionDate,
    @NotNull LocalTime admissionTime,
    @NotNull Long consultantId,
    @NotBlank String ward,
    @NotBlank String bedNumber,
    @NotBlank String provisionalDiagnosis,
    @Valid @NotNull AttendantRequest attendant) {}
