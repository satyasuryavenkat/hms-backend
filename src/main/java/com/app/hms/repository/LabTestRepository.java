package com.app.hms.repository;

import com.app.hms.entity.LabTest;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface LabTestRepository extends JpaRepository<LabTest, Long> {
  @Query(
      "select t from LabTest t where (:q='' or lower(t.name) like lower(concat('%',:q,'%')) or lower(t.code) like lower(concat('%',:q,'%')))")
  List<LabTest> searchAll(String q);

  @Query(
      "select t from LabTest t where (:q='' or lower(t.name) like lower(concat('%',:q,'%')) or lower(t.code) like lower(concat('%',:q,'%'))) and lower(t.department)=lower(:dept)")
  List<LabTest> searchByDepartment(String q, String dept);

  @Query(
      "select t from LabTest t where (:q='' or lower(t.name) like lower(concat('%',:q,'%')) or lower(t.code) like lower(concat('%',:q,'%'))) and t.active=:active")
  List<LabTest> searchByActive(String q, boolean active);

  @Query(
      "select t from LabTest t where (:q='' or lower(t.name) like lower(concat('%',:q,'%')) or lower(t.code) like lower(concat('%',:q,'%'))) and lower(t.department)=lower(:dept) and t.active=:active")
  List<LabTest> searchByDepartmentAndActive(String q, String dept, boolean active);

  boolean existsByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
