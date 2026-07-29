package com.app.hms.dto.response;

import com.app.hms.common.Enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record IpdAdmissionResponse(
    Long id,
    String ipdNumber,
    PatientResponse patient,
    AdmissionType admissionType,
    LocalDateTime admissionDateTime,
    DoctorResponse consultant,
    String ward,
    String bedNumber,
    String provisionalDiagnosis,
    String attendantName,
    String attendantMobile,
    AdmissionStatus status,
    BigDecimal discount,
    PaymentMode finalPaymentMode,
    BigDecimal finalPaidAmount,
    OffsetDateTime settledAt,
    DischargeType dischargeType,
    OffsetDateTime dischargeDateTime,
    String finalDiagnosis,
    String clinicalSummary,
    String advice,
    LocalDate followUpDate,
    DoctorResponse followUpDoctor,
    List<MedicationResponse> medications,
    boolean dischargeDraft) {}
