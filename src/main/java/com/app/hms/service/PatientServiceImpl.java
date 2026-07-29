package com.app.hms.service;

import com.app.hms.common.NotFoundException;
import com.app.hms.common.PageUtils;
import com.app.hms.dao.PatientDao;
import com.app.hms.dto.request.PatientRequest;
import com.app.hms.dto.response.*;
import com.app.hms.entity.Patient;
import com.app.hms.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientServiceImpl implements PatientService {
  private final PatientDao dao;
  private final PatientMapper mapper;

  @Override
  @Transactional
  public PatientResponse create(PatientRequest request) {
    return mapper.toResponse(createEntity(request));
  }

  @Override
  @Transactional
  public Patient createEntity(PatientRequest request) {
    Patient patient = new Patient();
    mapper.update(patient, request);
    patient = dao.save(patient);
    patient.setUhid("UHID-%06d".formatted(patient.getId()));
    return dao.save(patient);
  }

  @Override
  @Transactional
  public PatientResponse update(Long id, PatientRequest request) {
    Patient patient = findEntityById(id);
    mapper.update(patient, request);
    return mapper.toResponse(dao.save(patient));
  }

  @Override
  public PatientResponse findResponseById(Long id) {
    return mapper.toResponse(findEntityById(id));
  }

  @Override
  public Patient findEntityById(Long id) {
    return dao.findById(id).orElseThrow(() -> new NotFoundException("Patient not found"));
  }

  @Override
  public PatientSearchResponse search(String query, int page, int size) {
    Page<Patient> result = dao.search(PageUtils.query(query), PageUtils.request(page, size));
    return new PatientSearchResponse(
        result.stream().map(mapper::toResponse).toList(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }
}
