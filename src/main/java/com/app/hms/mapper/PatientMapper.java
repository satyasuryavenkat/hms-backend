package com.app.hms.mapper;

import com.app.hms.dto.request.PatientRequest;
import com.app.hms.dto.response.PatientResponse;
import com.app.hms.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
  public PatientResponse toResponse(Patient patient) {
    return new PatientResponse(
        patient.getId(),
        patient.getUhid(),
        patient.getName(),
        patient.getMobile(),
        patient.getDateOfBirth(),
        patient.calculateAge(),
        patient.getGender(),
        patient.getAddress(),
        patient.getCreatedAt());
  }

  public void update(Patient patient, PatientRequest request) {
    patient.setName(request.name());
    patient.setMobile(request.mobile());
    patient.setDateOfBirth(request.dateOfBirth());
    patient.setGender(request.gender());
    patient.setAddress(request.address());
  }
}
