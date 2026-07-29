package com.app.hms.dao;

import com.app.hms.entity.LabTest;
import com.app.hms.repository.LabTestRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LabTestDaoImpl implements LabTestDao {
  private final LabTestRepository repository;

  public List<LabTest> search(String q, String d, Boolean a) {
    return repository.search(q, d, a);
  }

  public Optional<LabTest> findById(Long id) {
    return repository.findById(id);
  }

  public boolean existsByCode(String code) {
    return repository.existsByCodeIgnoreCase(code);
  }

  public boolean existsByCode(String code, Long excludedId) {
    return repository.existsByCodeIgnoreCaseAndIdNot(code, excludedId);
  }

  public LabTest save(LabTest t) {
    return repository.save(t);
  }
}
