package com.app.hms.mapper;

import com.app.hms.dto.response.*;
import com.app.hms.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IpdMapper {
  private final PatientMapper patients;
  private final DoctorMapper doctors;

  public IpdAdmissionResponse admission(IpdAdmission a) {
    return new IpdAdmissionResponse(
        a.getId(),
        a.getIpdNumber(),
        patients.toResponse(a.getPatient()),
        a.getAdmissionType(),
        a.getAdmissionDateTime(),
        doctors.toResponse(a.getConsultant()),
        a.getWard(),
        a.getBedNumber(),
        a.getProvisionalDiagnosis(),
        a.getAttendantName(),
        a.getAttendantMobile(),
        a.getStatus(),
        a.getDiscount(),
        a.getFinalPaymentMode(),
        a.getFinalPaidAmount(),
        a.getSettledAt(),
        a.getDischargeType(),
        a.getDischargeDateTime(),
        a.getFinalDiagnosis(),
        a.getClinicalSummary(),
        a.getAdvice(),
        a.getFollowUpDate(),
        a.getFollowUpDoctor() == null ? null : doctors.toResponse(a.getFollowUpDoctor()),
        a.getMedications().stream()
            .map(
                m ->
                    new MedicationResponse(
                        m.getMedicine(),
                        m.getDose(),
                        m.getFrequency(),
                        m.getDuration(),
                        m.getInstructions()))
            .toList(),
        a.isDischargeDraft());
  }

  public IpdChargeResponse charge(IpdCharge c) {
    return new IpdChargeResponse(
        c.getId(),
        c.getCategory(),
        c.getDepartment(),
        c.getServiceCode(),
        c.getDescription(),
        c.getQuantity(),
        c.getRate(),
        c.getAmount());
  }

  public IpdAdvanceResponse advance(IpdAdvance a) {
    return new IpdAdvanceResponse(
        a.getId(),
        a.getReceiptNumber(),
        a.getAmount(),
        a.getPaymentMode(),
        a.getReferenceNumber(),
        a.getRemarks(),
        a.getCreatedAt());
  }
}
