package com.app.hms.repository;

import com.app.hms.entity.IpdAdvance;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdAdvanceRepository extends JpaRepository<IpdAdvance, Long> {
  List<IpdAdvance> findByAdmissionId(Long id);
}
