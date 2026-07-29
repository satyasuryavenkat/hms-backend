package com.app.hms.dao;

import com.app.hms.entity.OpVisit;
import com.app.hms.repository.OpVisitRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OpVisitDaoImpl implements OpVisitDao {
  private final OpVisitRepository repository;

  public OpVisit save(OpVisit visit) {
    return repository.save(visit);
  }

  public Optional<OpVisit> findById(Long id) {
    return repository.findById(id);
  }

  public Page<OpVisit> search(String query, LocalDate date, Pageable pageable) {
    return repository.search(query, date, pageable);
  }
}
