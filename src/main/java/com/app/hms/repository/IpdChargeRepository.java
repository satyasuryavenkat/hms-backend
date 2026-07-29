package com.app.hms.repository;

import com.app.hms.entity.IpdCharge;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdChargeRepository extends JpaRepository<IpdCharge, Long> {
  List<IpdCharge> findByAdmissionId(Long id);

  Optional<IpdCharge> findByIdAndAdmissionId(Long id, Long admissionId);
}
