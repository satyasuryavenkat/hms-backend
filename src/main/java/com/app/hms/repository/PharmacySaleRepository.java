package com.app.hms.repository;

import com.app.hms.entity.PharmacySale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacySaleRepository extends JpaRepository<PharmacySale, Long> {
  @org.springframework.data.jpa.repository.Query("""
      select s from PharmacySale s left join s.patient p
      where lower(s.invoiceNumber) like lower(concat('%', :query, '%'))
         or lower(s.customerName) like lower(concat('%', :query, '%'))
         or s.customerMobile like concat('%', :query, '%')
         or lower(p.uhid) like lower(concat('%', :query, '%'))
         or lower(p.name) like lower(concat('%', :query, '%'))
      order by s.createdAt desc, s.id desc
      """)
  org.springframework.data.domain.Page<PharmacySale> search(
      @org.springframework.data.repository.query.Param("query") String query,
      org.springframework.data.domain.Pageable pageable);
}
