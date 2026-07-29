package com.app.hms.dao;

import com.app.hms.entity.OpVisit;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.*;

public interface OpVisitDao {
  OpVisit save(OpVisit visit);

  Optional<OpVisit> findById(Long id);

  Page<OpVisit> search(String query, LocalDate date, Pageable pageable);
}
