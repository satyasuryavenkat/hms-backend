package com.app.hms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.app.hms.entity.PharmacySale;
import com.app.hms.repository.PharmacySaleRepository;
import com.app.hms.security.RemoteApiGate;
import com.app.hms.service.PharmacyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PharmacyHistoryTests {
  @Autowired PharmacySaleRepository sales;
  @Autowired PharmacyService service;
  @MockitoBean RemoteApiGate gate;

  @Test
  void findsWalkInInvoicesAndPaginatesSavedSales() {
    for (int i = 0; i < 3; i++) {
      var sale = new PharmacySale();
      sale.setInvoiceNumber("HISTORY-TEST-" + i);
      sale.setCustomerName("History Customer");
      sale.setCustomerMobile("9765401122");
      sales.saveAndFlush(sale);
    }
    var page = service.searchSales("history-test", 1, 2);
    assertEquals(3, page.totalElements());
    assertEquals(2, page.totalPages());
    assertEquals(1, page.content().size());
    assertEquals(3, service.searchSales("History Customer", 0, 20).totalElements());
    assertEquals(3, service.searchSales("9765401122", 0, 20).totalElements());
    assertEquals(0, service.searchSales("missing-history-invoice", 0, 20).totalElements());
  }
}
