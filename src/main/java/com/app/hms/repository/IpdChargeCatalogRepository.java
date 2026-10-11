package com.app.hms.repository;

import com.app.hms.entity.IpdChargeCatalogItem;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdChargeCatalogRepository extends JpaRepository<IpdChargeCatalogItem, Long> {
  List<IpdChargeCatalogItem> findAllByOrderByCategoryLabelAscServiceNameAsc();

  List<IpdChargeCatalogItem> findByActiveTrueOrderByCategoryLabelAscServiceNameAsc();

  Optional<IpdChargeCatalogItem>
      findByCategoryIgnoreCaseAndDepartmentIgnoreCaseAndServiceNameIgnoreCase(
          String category, String department, String serviceName);

  boolean existsByCategoryIgnoreCaseAndDepartmentIgnoreCaseAndServiceNameIgnoreCase(
      String category, String department, String serviceName);

  boolean existsByCategoryIgnoreCaseAndDepartmentIgnoreCaseAndServiceNameIgnoreCaseAndIdNot(
      String category, String department, String serviceName, Long id);

  boolean existsByServiceCodeIgnoreCase(String serviceCode);
}
