package com.app.hms.dao;

import com.app.hms.entity.Patient;
import java.util.Optional;
import org.springframework.data.domain.*;

public interface PatientDao {
  Patient save(Patient patient);

  Optional<Patient> findById(Long id);

  Page<Patient> search(String query, Pageable pageable);
}
