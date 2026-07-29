package com.app.hms.repository;

import com.app.hms.entity.Patient;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface PatientRepository extends JpaRepository<Patient, Long> {
  @Query(
      "select p from Patient p where lower(p.name) like lower(concat('%',:query,'%')) "
          + "or lower(p.uhid) like lower(concat('%',:query,'%')) or p.mobile like concat('%',:query,'%')")
  Page<Patient> search(String query, Pageable pageable);
}
