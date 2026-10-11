package com.app.hms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.app.hms.security.RemoteApiGate;
import com.app.hms.service.OverviewService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class OverviewTests {
  @Autowired OverviewService overview;
  @Autowired JdbcTemplate jdbc;
  @Autowired WebApplicationContext context;
  @MockitoBean RemoteApiGate gate;
  final LocalDate date = LocalDate.of(2040, 4, 1);

  @Test
  void aggregatesWithoutJoinMultiplicationAndUsesInclusiveHospitalDates() {
    var start = OffsetDateTime.parse("2040-04-01T00:00:00+05:30");
    jdbc.update("INSERT INTO patients(id,name,mobile,gender,created_at) VALUES(90001,'Overview','1234567890','MALE',?)", start);
    jdbc.update("INSERT INTO doctors(id,doctor_code,name,active) VALUES(90001,'OVERVIEW','Overview',true)");
    for (int i=0; i<3; i++) {
      var time = i==0 ? start : i==1 ? start.plusDays(1).minusNanos(1000) : start.plusDays(1);
      jdbc.update("INSERT INTO op_visit(id,patient_id,doctor_id,total_payable,paid_amount,created_at) VALUES(?,90001,90001,100,80,?)",90001+i,time);
    }
    jdbc.update("INSERT INTO ipd_admission(id,patient_id,consultant_id,care_type,admission_date_time,discount,final_paid_amount,discharge_draft) VALUES(90001,90001,90001,'IPD',?,50,30,false)",date.atStartOfDay());
    jdbc.update("INSERT INTO ipd_admission(id,patient_id,consultant_id,care_type,admission_date_time,discount,final_paid_amount,discharge_draft) VALUES(90002,90001,90001,'DAYCARE',?,0,0,false)",date.atTime(23,59));
    jdbc.update("INSERT INTO ipd_charge(id,admission_id,amount) VALUES(90001,90001,200),(90002,90001,300),(90003,90002,70)");
    jdbc.update("INSERT INTO ipd_advance(id,admission_id,amount) VALUES(90001,90001,100),(90002,90001,150)");
    jdbc.update("INSERT INTO lab_order(id,patient_id,total_payable,paid_amount,report_status,created_at) VALUES(90001,90001,250,200,'PENDING',?),(90002,90001,999,999,'CANCELLED',?)",start,start);
    jdbc.update("INSERT INTO lab_order_item(id,order_id,amount) VALUES(90001,90001,100),(90002,90001,200),(90003,90002,999)");
    var result = overview.get(date,date);
    assertEquals(1,result.newPatients());
    assertEquals(1,result.patientsServed());
    assertEquals(2,result.departments().get(0).count());
    assertEquals(1,result.departments().get(1).count());
    assertEquals(1,result.departments().get(2).count());
    assertEquals(2,result.departments().get(3).count());
    assertEquals(1,result.departments().get(3).records());
    assertEquals(0,new BigDecimal("450").compareTo(result.departments().get(1).billed()));
    assertEquals(0,new BigDecimal("280").compareTo(result.departments().get(1).collected()));
    assertEquals(0,new BigDecimal("970").compareTo(result.billed()));
    assertEquals(0,new BigDecimal("640").compareTo(result.collected()));
  }

  @Test
  void emptyRangeHasZeroTotals() {
    var result = overview.get(date,date);
    assertEquals(0,result.patientsServed());
    assertEquals(0,result.billed().signum());
    assertTrue(result.departments().stream().allMatch(item -> item.count()==0));
  }

  @Test
  void endpointRequiresAdministratorAndValidDates() throws Exception {
    when(gate.isEnabled()).thenReturn(true);
    var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    mvc.perform(get("/api/v1/overview").param("startDate","2040-04-01").param("endDate","2040-04-01")
        .with(user("staff").roles("RECEPTIONIST"))).andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/overview").param("startDate","2040-04-01").param("endDate","2040-04-01")
        .with(user("admin").roles("ADMINISTRATOR"))).andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.departments.length()").value(4));
    mvc.perform(get("/api/v1/overview").param("startDate","2040-04-02").param("endDate","2040-04-01")
        .with(user("admin").roles("ADMINISTRATOR"))).andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/overview").with(user("admin").roles("ADMINISTRATOR")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void currentBedsIncludesActiveAndDischargePlannedPatientsAcrossAdmissionDates() {
    jdbc.update("INSERT INTO patients(id,name,mobile,gender,age,uhid,created_at) VALUES(90005,'Bed Patient','9000000000','FEMALE',42,'BED-UHID',?)", OffsetDateTime.now());
    jdbc.update("INSERT INTO doctors(id,doctor_code,name,active) VALUES(90005,'BED-DOC','Bed Consultant',true)");
    String[] statuses = {"ACTIVE","DISCHARGE_PLANNED","DISCHARGED","ACTIVE","ACTIVE"};
    String[] beds = {"G-01","D-02","G-03","",null};
    for (int i=0; i<statuses.length; i++) {
      jdbc.update("INSERT INTO ipd_admission(id,patient_id,consultant_id,care_type,admission_date_time,status,ward,bed_number,ipd_number,provisional_diagnosis,attendant_name,attendant_mobile,discharge_draft) VALUES(?,90005,90005,?,?,?,?,?,?,'Observation','Attendant','9111111111',false)",
          90010+i, i==1 ? "DAYCARE" : "IPD", date.minusYears(10).atTime(10,30), statuses[i], "General", beds[i], "BED-"+i);
    }
    var patients = overview.currentBeds().stream().filter(patient -> patient.admissionId() >= 90010 && patient.admissionId() <= 90014).toList();
    assertEquals(2, patients.size());
    var ipd = patients.stream().filter(p -> p.careType().equals("IPD")).findFirst().orElseThrow();
    assertEquals("Bed Patient", ipd.patientName());
    assertEquals("G-01", ipd.bedNumber());
    assertEquals("BED-UHID", ipd.uhid());
    assertEquals(42, ipd.age());
    assertEquals("Bed Consultant", ipd.consultantName());
    assertEquals("Attendant", ipd.attendantName());
    assertTrue(patients.stream().anyMatch(p -> p.status().equals("DISCHARGE_PLANNED")));
  }

  @Test
  void currentBedsEndpointIsAdministratorOnly() throws Exception {
    when(gate.isEnabled()).thenReturn(true);
    var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    mvc.perform(get("/api/v1/overview/current-beds").with(user("staff").roles("RECEPTIONIST")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/overview/current-beds").with(user("admin").roles("ADMINISTRATOR")))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
  }
}
