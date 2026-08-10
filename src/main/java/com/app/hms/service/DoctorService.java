package com.app.hms.service;

import com.app.hms.dto.request.DoctorRequest;
import com.app.hms.dto.response.DoctorResponse;
import com.app.hms.entity.Doctor;
import java.util.List;

public interface DoctorService {
  List<DoctorResponse> findAll(Boolean active, String department);

  DoctorResponse findResponseById(Long id);

  Doctor findEntityById(Long id);

  DoctorResponse create(DoctorRequest request);

  DoctorResponse update(Long id, DoctorRequest request);

  void delete(Long id);
}
