package com.app.hms.repository;

import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.entity.LabOrder;
import java.time.*;
import java.util.Collection;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
  @Query(
      "select distinct o from LabOrder o where (:q='' or lower(o.patient.name) like lower(concat('%',:q,'%')) or lower(o.patient.uhid) like lower(concat('%',:q,'%')) or o.patient.mobile like concat('%',:q,'%') or lower(o.orderNumber) like lower(concat('%',:q,'%')))")
  Page<LabOrder> searchAll(String q, Pageable page);

  @Query(
      "select distinct o from LabOrder o where (:q='' or lower(o.patient.name) like lower(concat('%',:q,'%')) or lower(o.patient.uhid) like lower(concat('%',:q,'%')) or o.patient.mobile like concat('%',:q,'%') or lower(o.orderNumber) like lower(concat('%',:q,'%'))) and o.reportStatus=:status")
  Page<LabOrder> searchByStatus(String q, LabReportStatus status, Pageable page);

  @Query(
      "select distinct o from LabOrder o where (:q='' or lower(o.patient.name) like lower(concat('%',:q,'%')) or lower(o.patient.uhid) like lower(concat('%',:q,'%')) or o.patient.mobile like concat('%',:q,'%') or lower(o.orderNumber) like lower(concat('%',:q,'%'))) and date(o.createdAt)=:date")
  Page<LabOrder> searchByDate(String q, LocalDate date, Pageable page);

  @Query(
      "select distinct o from LabOrder o where (:q='' or lower(o.patient.name) like lower(concat('%',:q,'%')) or lower(o.patient.uhid) like lower(concat('%',:q,'%')) or o.patient.mobile like concat('%',:q,'%') or lower(o.orderNumber) like lower(concat('%',:q,'%'))) and o.reportStatus=:status and date(o.createdAt)=:date")
  Page<LabOrder> searchByStatusAndDate(
      String q, LabReportStatus status, LocalDate date, Pageable page);

  @Query(
      "select distinct o from LabOrder o where (:q='' or lower(o.patient.name) like lower(concat('%',:q,'%')) or lower(o.patient.uhid) like lower(concat('%',:q,'%')) or lower(o.patient.uhid) like lower(concat('%',:q,'%')) or o.patient.mobile like concat('%',:q,'%') or lower(o.orderNumber) like lower(concat('%',:q,'%'))) and o.reportStatus in :statuses order by o.createdAt desc")
  Page<LabOrder> searchCompletedReports(
      String q, Collection<LabReportStatus> statuses, Pageable page);
}
