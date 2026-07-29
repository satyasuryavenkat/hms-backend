package com.app.hms.mapper;

import com.app.hms.dto.response.OpVisitResponse;
import com.app.hms.entity.OpVisit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpVisitMapper {
  private final PatientMapper patientMapper;
  private final DoctorMapper doctorMapper;

  public OpVisitResponse toResponse(OpVisit v) {
    return new OpVisitResponse(
        v.getId(),
        v.getOpNumber(),
        v.getReceiptNumber(),
        patientMapper.toResponse(v.getPatient()),
        doctorMapper.toResponse(v.getDoctor()),
        v.getVisitType(),
        v.getAppointmentDate(),
        v.getAppointmentTime(),
        v.getSymptoms(),
        v.getConsultationFee(),
        v.getDiscount(),
        v.getTotalPayable(),
        v.getPaidAmount(),
        v.getPaymentMode(),
        v.getPaymentStatus(),
        v.getCreatedAt());
  }
}
