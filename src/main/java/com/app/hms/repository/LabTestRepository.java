package com.app.hms.repository;

import com.app.hms.entity.LabTest;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface LabTestRepository extends JpaRepository<LabTest, Long> {
  @Query(
      "select t from LabTest t where (:q='' or lower(t.name) like lower(concat('%',:q,'%')) or lower(t.code) like lower(concat('%',:q,'%'))) and (:dept is null or lower(t.department)=lower(:dept)) and (:active is null or t.active=:active)")
  List<LabTest> search(String q, String dept, Boolean active);

  boolean existsByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
