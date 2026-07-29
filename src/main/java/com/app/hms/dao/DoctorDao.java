package com.app.hms.dao;

import com.app.hms.entity.Doctor;
import java.util.List;
import java.util.Optional;

public interface DoctorDao {
  List<Doctor> findAll(Boolean active, String department);

  Optional<Doctor> findById(Long id);

  Doctor save(Doctor doctor);

  boolean existsByCode(String code, Long excludedId);

  long count();
}
