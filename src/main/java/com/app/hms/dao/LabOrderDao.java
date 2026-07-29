package com.app.hms.dao;

import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.entity.LabOrder;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.*;

public interface LabOrderDao {
  LabOrder save(LabOrder order);

  Optional<LabOrder> findById(Long id);

  Page<LabOrder> search(String query, LabReportStatus status, LocalDate date, Pageable pageable);
}
