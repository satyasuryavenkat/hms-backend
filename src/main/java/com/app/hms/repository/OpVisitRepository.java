package com.app.hms.repository;

import com.app.hms.entity.OpVisit;
import java.time.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface OpVisitRepository extends JpaRepository<OpVisit, Long> {
  @Query(
      "select v from OpVisit v where (:date is null or v.appointmentDate=:date) and (:q='' or lower(v.patient.name) like lower(concat('%',:q,'%')) or lower(v.opNumber) like lower(concat('%',:q,'%')))")
  Page<OpVisit> search(String q, LocalDate date, Pageable p);
}
