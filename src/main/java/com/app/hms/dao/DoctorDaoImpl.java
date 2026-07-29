package com.app.hms.dao;

import com.app.hms.entity.Doctor;
import com.app.hms.repository.DoctorRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DoctorDaoImpl implements DoctorDao {
  private final DoctorRepository repository;

  @Override
  public List<Doctor> findAll(Boolean active, String department) {
    if (active != null && department != null) {
      return repository.findByActiveAndDepartmentIgnoreCase(active, department);
    }
    if (active != null) return repository.findByActive(active);
    if (department != null) return repository.findByDepartmentIgnoreCase(department);
    return repository.findAll();
  }

  @Override
  public Optional<Doctor> findById(Long id) {
    return repository.findById(id);
  }

  @Override
  public Doctor save(Doctor doctor) {
    return repository.save(doctor);
  }

  @Override
  public boolean existsByCode(String code, Long excludedId) {
    return excludedId == null
        ? repository.existsByDoctorCodeIgnoreCase(code)
        : repository.existsByDoctorCodeIgnoreCaseAndIdNot(code, excludedId);
  }

  @Override
  public long count() {
    return repository.count();
  }
}
