package com.app.hms;

import static org.junit.jupiter.api.Assertions.*;
import com.app.hms.common.BadRequestException;
import com.app.hms.common.Enums.PaymentMode;
import com.app.hms.dto.request.FinalBillSettlementRequest;
import com.app.hms.dto.response.OpVisitResponse;
import com.app.hms.service.*;
import com.app.hms.security.RemoteApiGate;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BillingRecordsTests {
  @Autowired BillingRecordsService records;
  @Autowired IpdService ipd;
  @Autowired jakarta.persistence.EntityManager em;
  @Autowired JdbcTemplate jdbc;
  @Autowired org.springframework.web.context.WebApplicationContext context;
  @MockitoBean RemoteApiGate gate;
  final LocalDate day = LocalDate.of(2042, 1, 2);
  @Test void correctsDatesForSettledAndDischargedVisitsWithoutChangingBilling() {
    for (long id : new long[]{97001, 97002}) {
      jdbc.update("UPDATE ipd_admission SET status='DISCHARGED',final_paid_amount=300,discount=0 WHERE id=?", id);
      var before = ipd.finalBill(id);
      var request = new com.app.hms.dto.request.AdmissionDatesRequest(day.minusDays(2).atTime(9,0), day.atTime(18,0).atOffset(ZoneOffset.ofHoursMinutes(5,30)));
      ipd.updateDates(id, request);
      em.flush(); em.clear();
      var admission = id == 97001 ? ipd.findById(id) : ipd.findDaycareById(id);
      assertEquals(request.admissionDateTime(), admission.admissionDateTime());
      assertEquals(request.dischargeDateTime().toInstant(), admission.dischargeDateTime().toInstant());
      assertEquals(before.paidAmount(), ipd.finalBill(id).paidAmount());
      assertEquals(before.settledAt().toInstant(), ipd.finalBill(id).settledAt().toInstant());
      assertThrows(BadRequestException.class, () -> ipd.updateDates(id, new com.app.hms.dto.request.AdmissionDatesRequest(day.atTime(19,0), request.dischargeDateTime())));
      assertThrows(BadRequestException.class, () -> ipd.updateDates(id, new com.app.hms.dto.request.AdmissionDatesRequest(request.admissionDateTime(), null)));
    }
  }
  @Test void historyRoutesAndOverviewDetailsRespectModuleAccess() throws Exception {
    org.mockito.Mockito.when(gate.isEnabled()).thenReturn(true);
    var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
        .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
    for (String path : new String[]{"op-visits/history", "lab-orders/history", "ipd-admissions/history", "daycare-admissions/history", "pharmacy/sales/history"}) {
      mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/" + path)
          .servletPath("/api/v1/" + path).param("startDate", day.toString()).param("endDate", day.toString())
          .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRATOR")))
          .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
          .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.content[0].record").exists());
    }
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/overview/op/records")
        .param("startDate",day.toString()).param("endDate",day.toString())
        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("staff").roles("RECEPTIONIST")))
        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/overview/op/records")
        .param("startDate",day.toString()).param("endDate",day.toString())
        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRATOR")))
        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.totalElements").value(2));
  }
  @Test void savedDraftsRemainEditableAndEnterHistoryOnlyAfterSettlement() {
    for (long id : new long[]{97001,97002}) {
      jdbc.update("UPDATE ipd_admission SET settled_at=null WHERE id=?", id);
      String section = id == 97001 ? "ipd" : "daycare";
      assertEquals(0, records.records(section,"range",null,null,0,20,false).totalElements());
      var request = new com.app.hms.dto.request.DischargeRequest(day.atTime(15,0).atOffset(ZoneOffset.ofHoursMinutes(5,30)), com.app.hms.common.Enums.DischargeType.ROUTINE, "Saved diagnosis", "Saved course", java.util.List.of(), "Saved advice", day.plusDays(7), 97001L);
      var saved = ipd.saveDischarge(id, request);
      em.flush(); em.clear();
      assertNotNull(saved.dischargeDraftSavedAt());
      var savedDay = saved.dischargeDraftSavedAt().atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDate();
      assertEquals(0, records.records(section,"range",null,null,0,20,false).totalElements());
      var restored = id == 97001 ? ipd.findById(id) : ipd.findDaycareById(id);
      assertEquals("Saved course", restored.clinicalSummary());
      assertEquals("Saved advice", restored.advice());
      assertNull(restored.settledAt());
      assertEquals(0, records.records(section,"range",savedDay.minusDays(1),savedDay.minusDays(1),0,20,false).totalElements());
      ipd.saveDischarge(id, new com.app.hms.dto.request.DischargeRequest(request.dischargeDateTime(), request.dischargeType(), "Edited diagnosis", "Edited course", java.util.List.of(), "Edited advice", request.followUpDate(), 97001L));
      em.flush(); em.clear();
      assertEquals("Edited course", (id == 97001 ? ipd.findById(id) : ipd.findDaycareById(id)).clinicalSummary());
      assertEquals(0, records.records(section,"range",null,null,0,20,false).totalElements());
      jdbc.update("UPDATE ipd_admission SET settled_at=? WHERE id=?", day.atTime(12,0).atOffset(ZoneOffset.ofHoursMinutes(5,30)), id);
      em.clear();
      assertEquals(1, records.records(section,"range",day,day,0,20,false).totalElements());
    }
  }
  @BeforeEach void fixtures() {
    jdbc.update("INSERT INTO patients(id,name,uhid,mobile,age,gender,created_at) VALUES(97001,'Range Patient','RANGE-UHID','9000000000',30,'MALE',CURRENT_TIMESTAMP)");
    jdbc.update("INSERT INTO doctors(id,doctor_code,name,active) VALUES(97001,'RANGE-DOC','Range Doctor',true)");
    for (int i = 0; i < 4; i++) {
      var time = day.atStartOfDay(ZoneId.of("Asia/Kolkata")).toOffsetDateTime().plusHours(i == 0 ? -1 : i == 3 ? 24 : i);
      jdbc.update("INSERT INTO op_visit(id,op_number,receipt_number,patient_id,doctor_id,payment_status,created_at) VALUES(?,?,?,97001,97001,'PAID',?)", 97001+i, "RANGE-OP-"+i, "RANGE-REC-"+i, time);
      jdbc.update("INSERT INTO pharmacy_sales(id,invoice_number,customer_name,payment_status,created_at) VALUES(?,?,'Range Walkin','PAID',?)", 97001+i, "RANGE-PH-"+i, time);
      jdbc.update("INSERT INTO lab_order(id,order_number,patient_id,report_status,payment_status,created_at) VALUES(?,?,97001,?,'PAID',?)", 97001+i, "RANGE-LAB-"+i, i == 2 ? "CANCELLED" : "PENDING", time);
    }
    for (int i = 0; i < 2; i++) {
      jdbc.update("INSERT INTO ipd_admission(id,ipd_number,care_type,patient_id,consultant_id,status,admission_date_time,settled_at,discount,final_paid_amount,discharge_draft) VALUES(?,?,?,97001,97001,'ACTIVE',?,?,0,0,false)", 97001+i, "RANGE-ADM-"+i, i == 0 ? "IPD" : "DAYCARE", day.minusDays(1).atTime(10,0), day.atTime(12,0).atOffset(ZoneOffset.ofHoursMinutes(5,30)));
      for (int c = 0; c < 3; c++) jdbc.update("INSERT INTO ipd_charge(id,admission_id,category,description,quantity,rate,amount) VALUES(?,?,?,?,1,100,100)", 97100+i*3+c,97001+i, new String[]{"ROOM","LAB_TESTS","PHARMACY"}[c], "Range charge");
    }
  }
  @Test void filtersInclusiveHospitalDatesBeforePaginationAndExcludesCancelledLabs() {
    var page = records.records("op", "range", day, day, 1, 1, false);
    assertEquals(2, page.totalElements());
    assertEquals(2, page.totalPages());
    assertEquals("RANGE-OP-1", ((OpVisitResponse)page.content().get(0).record()).opNumber());
    assertEquals(2, records.records("pharmacy", "Range Walkin", day, day, 0,20,false).totalElements());
    assertEquals(1, records.records("lab", "range", day, day,0,20,false).totalElements());
    for (String section : new String[]{"ipd","daycare"}) {
      assertEquals(1, records.records(section,"range",day,day,0,20,false).totalElements());
      assertEquals(0, records.records(section,"range",day,day,0,20,true).totalElements());
      assertEquals(1, records.records(section,"range",day.minusDays(1),day.minusDays(1),0,20,true).totalElements());
    }
    assertThrows(BadRequestException.class, () -> records.records("op","",day,day.minusDays(1),0,20,false));
  }
  @Test void sectionDiscountsPersistForBothCareTypesAndRejectExcessDiscounts() {
    for (long id : new long[]{97001,97002}) {
      var request = new FinalBillSettlementRequest(new BigDecimal("65"), "Approved", PaymentMode.CASH, new BigDecimal("235"), null, new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"), day.atTime(18,0).atOffset(ZoneOffset.ofHoursMinutes(5,30)));
      assertThrows(BadRequestException.class, () -> ipd.settle(id, new FinalBillSettlementRequest(new BigDecimal("65"), "Approved", PaymentMode.CASH, new BigDecimal("235"), null, new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"), day.minusDays(2).atStartOfDay().atOffset(ZoneOffset.UTC))));
      ipd.settle(id, request);
      em.flush();
      em.clear();
      var saved = ipd.finalBill(id);
      assertNotNull((id == 97001 ? ipd.findById(id) : ipd.findDaycareById(id)).dischargeDateTime());
      assertEquals(0, saved.discount().compareTo(new BigDecimal("65")));
      assertEquals(0, saved.hospitalDiscount().compareTo(new BigDecimal("10")));
      assertEquals(0, saved.laboratoryDiscount().compareTo(new BigDecimal("20")));
      assertEquals(0, saved.pharmacyDiscount().compareTo(new BigDecimal("30")));
      assertEquals(0, saved.balancePayable().compareTo(BigDecimal.ZERO));
      assertThrows(BadRequestException.class, () -> ipd.settle(id, new FinalBillSettlementRequest(new BigDecimal("101"), "", PaymentMode.CASH, new BigDecimal("199"), null, new BigDecimal("101"), BigDecimal.ZERO, BigDecimal.ZERO, null)));
    }
  }
}
