package com.app.hms.repository;

import com.app.hms.common.Enums.AdmissionStatus;
import com.app.hms.entity.IpdAdmission;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface IpdAdmissionRepository extends JpaRepository<IpdAdmission, Long> {
  @Query(
      "select a from IpdAdmission a where (:status is null or a.status=:status) and (:q='' or lower(a.patient.name) like lower(concat('%',:q,'%')) or lower(a.patient.uhid) like lower(concat('%',:q,'%')) or lower(a.ipdNumber) like lower(concat('%',:q,'%')) or a.patient.mobile like concat('%',:q,'%') or lower(a.ward) like lower(concat('%',:q,'%')) or lower(a.bedNumber) like lower(concat('%',:q,'%')))")
  List<IpdAdmission> search(String q, AdmissionStatus status);

  boolean existsByBedNumberAndStatus(String bed, AdmissionStatus status);
}
