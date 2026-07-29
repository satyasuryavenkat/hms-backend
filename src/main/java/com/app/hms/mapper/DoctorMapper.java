package com.app.hms.mapper;

import com.app.hms.dto.response.DoctorResponse;
import com.app.hms.entity.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {
  public DoctorResponse toResponse(Doctor doctor) {
    return new DoctorResponse(
        doctor.getId(),
        doctor.getDoctorCode(),
        doctor.getName(),
        doctor.getDepartment(),
        doctor.getSpecialization(),
        doctor.getConsultationFee(),
        doctor.isActive());
  }
}
