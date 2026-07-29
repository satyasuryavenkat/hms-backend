package com.app.hms.dao;

import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.entity.LabOrder;
import com.app.hms.repository.LabOrderRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LabOrderDaoImpl implements LabOrderDao {
  private final LabOrderRepository repository;

  public LabOrder save(LabOrder o) {
    return repository.save(o);
  }

  public Optional<LabOrder> findById(Long id) {
    return repository.findById(id);
  }

  public Page<LabOrder> search(String q, LabReportStatus s, LocalDate d, Pageable p) {
    return repository.search(q, s, d, p);
  }
}
