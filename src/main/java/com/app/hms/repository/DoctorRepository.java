package com.app.hms.repository;

import com.app.hms.entity.Doctor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
  List<Doctor> findByActiveAndDepartmentIgnoreCase(boolean active, String department);

  List<Doctor> findByActive(boolean active);

  List<Doctor> findByDepartmentIgnoreCase(String department);

  boolean existsByDoctorCodeIgnoreCase(String doctorCode);

  boolean existsByDoctorCodeIgnoreCaseAndIdNot(String doctorCode, Long id);
}
