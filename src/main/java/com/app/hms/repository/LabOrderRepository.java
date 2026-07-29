package com.app.hms.repository;

import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.entity.LabOrder;
import java.time.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
  @Query(
      "select distinct o from LabOrder o where (:q='' or lower(o.patient.name) like lower(concat('%',:q,'%')) or lower(o.orderNumber) like lower(concat('%',:q,'%'))) and (:status is null or o.reportStatus=:status) and (:date is null or date(o.createdAt)=:date)")
  Page<LabOrder> search(String q, LabReportStatus status, LocalDate date, Pageable page);
}
