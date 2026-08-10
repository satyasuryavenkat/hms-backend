package com.app.hms.dao;

import com.app.hms.entity.LabTest;
import java.util.*;

public interface LabTestDao {
  List<LabTest> search(String query, String department, Boolean active);

  Optional<LabTest> findById(Long id);

  boolean existsByCode(String code);

  boolean existsByCode(String code, Long excludedId);

  LabTest save(LabTest test);

  void delete(LabTest test);
}
