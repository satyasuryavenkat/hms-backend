package com.app.hms.dao;

import com.app.hms.entity.Patient;
import com.app.hms.repository.PatientRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PatientDaoImpl implements PatientDao {
  private final PatientRepository repository;

  @Override
  public Patient save(Patient patient) {
    return repository.save(patient);
  }

  @Override
  public Optional<Patient> findById(Long id) {
    return repository.findById(id);
  }

  @Override
  public Page<Patient> search(String query, Pageable pageable) {
    return repository.search(query, pageable);
  }
}
