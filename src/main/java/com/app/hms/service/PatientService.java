package com.app.hms.service;

import com.app.hms.dto.request.PatientRequest;
import com.app.hms.dto.response.*;
import com.app.hms.entity.Patient;

public interface PatientService {
  PatientResponse create(PatientRequest request);

  PatientResponse update(Long id, PatientRequest request);

  PatientResponse findResponseById(Long id);

  Patient findEntityById(Long id);

  Patient createEntity(PatientRequest request);

  PatientSearchResponse search(String query, int page, int size);
}
