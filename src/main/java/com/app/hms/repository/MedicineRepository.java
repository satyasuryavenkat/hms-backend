package com.app.hms.repository;

import com.app.hms.entity.Medicine;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
  @Query(
      "select m from Medicine m where (:active is null or m.active = :active) and "
          + "(lower(m.name) like lower(concat('%',:query,'%')) or "
          + "lower(coalesce(m.genericName,'')) like lower(concat('%',:query,'%')) or "
          + "lower(m.manufacturerCode) like lower(concat('%',:query,'%')) or "
          + "lower(coalesce(m.type,'')) like lower(concat('%',:query,'%')) or "
          + "lower(coalesce(m.batchNumber,'')) like lower(concat('%',:query,'%'))) order by m.name")
  List<Medicine> search(@Param("query") String query, @Param("active") Boolean active);

  boolean existsByManufacturerCodeIgnoreCase(String code);

  boolean existsByManufacturerCodeIgnoreCaseAndIdNot(String code, Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from Medicine m where m.id = :id")
  Optional<Medicine> findByIdForUpdate(@Param("id") Long id);
}
