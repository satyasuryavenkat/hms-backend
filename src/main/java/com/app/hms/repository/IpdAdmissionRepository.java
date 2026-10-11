package com.app.hms.repository;

import com.app.hms.common.Enums.AdmissionStatus;
import com.app.hms.common.Enums.CareType;
import com.app.hms.entity.IpdAdmission;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface IpdAdmissionRepository extends JpaRepository<IpdAdmission, Long> {
  @Query(
      "select a from IpdAdmission a where (a.careType=:careType or (:includeLegacy=true and a.careType is null)) and (:status is null or a.status=:status) and (:q='' or lower(a.patient.name) like lower(concat('%',:q,'%')) or lower(a.patient.uhid) like lower(concat('%',:q,'%')) or lower(a.ipdNumber) like lower(concat('%',:q,'%')) or a.patient.mobile like concat('%',:q,'%') or lower(a.ward) like lower(concat('%',:q,'%')) or lower(a.bedNumber) like lower(concat('%',:q,'%')))" )
  List<IpdAdmission> search(
      String q, AdmissionStatus status, CareType careType, boolean includeLegacy);

  boolean existsByBedNumberAndCareTypeAndStatusIn(
      String bed, CareType careType, Collection<AdmissionStatus> statuses);
}
