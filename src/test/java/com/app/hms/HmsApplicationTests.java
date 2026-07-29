package com.app.hms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.hms.common.Enums.Gender;
import com.app.hms.common.Enums.LabPriority;
import com.app.hms.common.Enums.PaymentMode;
import com.app.hms.common.Enums.SpecimenType;
import com.app.hms.dto.request.CreateLabOrderRequest;
import com.app.hms.dto.request.CreatePharmacySaleRequest;
import com.app.hms.dto.request.LabParameterRequest;
import com.app.hms.dto.request.LabResultRequest;
import com.app.hms.dto.request.LabTestRequest;
import com.app.hms.dto.request.MedicineRequest;
import com.app.hms.entity.Patient;
import com.app.hms.repository.PatientRepository;
import com.app.hms.service.LabOrderService;
import com.app.hms.service.LabTestService;
import com.app.hms.service.PharmacyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class HmsApplicationTests {

  @Autowired private CorsConfigurationSource corsConfigurationSource;
  @Autowired private LabTestService labTestService;
  @Autowired private LabOrderService labOrderService;
  @Autowired private PharmacyService pharmacyService;
  @Autowired private PatientRepository patientRepository;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void contextLoads() {}

  @Test
  void allowsConfiguredLocalReactOrigin() {
    var cors =
        corsConfigurationSource.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/"));

    assertNotNull(cors);
    assertEquals("http://localhost:3000", cors.checkOrigin("http://localhost:3000"));
    assertEquals("http://localhost:5173", cors.checkOrigin("http://localhost:5173"));
    assertNotNull(cors.checkHttpMethod(HttpMethod.POST));
    assertEquals(Boolean.TRUE, cors.getAllowCredentials());
    assertNull(cors.checkOrigin("https://unconfigured.example"));
  }

  @Test
  void createsAndUpdatesLabTestWithReportTemplate() throws Exception {
    var created =
        labTestService.create(
            new LabTestRequest(
                "LAB-CRUD-TEST",
                "Test Profile",
                "Biochemistry",
                new BigDecimal("500"),
                24,
                SpecimenType.BLOOD,
                true,
                List.of(new LabParameterRequest(null, "Haemoglobin", "g/dL", "12 - 16"))));

    var updated =
        labTestService.update(
            created.id(),
            new LabTestRequest(
                "LAB-CRUD-TEST",
                "Updated Test Profile",
                "Biochemistry",
                new BigDecimal("650"),
                12,
                SpecimenType.BLOOD,
                true,
                List.of(
                    new LabParameterRequest(
                        created.parameters().get(0).parameterId(),
                        "Haemoglobin",
                        "g/dL",
                        "11 - 17"),
                    new LabParameterRequest(null, "WBC", "cells/cumm", "4000 - 11000"))));

    assertEquals("Updated Test Profile", updated.name());
    assertEquals(new BigDecimal("650"), updated.price());
    assertEquals(SpecimenType.BLOOD, updated.specimenType());
    assertEquals(2, updated.parameters().size());
    assertNotNull(updated.parameters().get(1).parameterId());

    Long parameterId = updated.parameters().get(0).parameterId();
    String json = objectMapper.writeValueAsString(updated.parameters().get(0));
    assertTrue(json.contains("\"parameterId\":\"" + parameterId + "\""));

    LabResultRequest resultRequest =
        objectMapper.readValue(
            "{\"parameterId\":\"" + parameterId + "\",\"result\":\"14.0\",\"abnormal\":false}",
            LabResultRequest.class);
    assertEquals(parameterId, resultRequest.parameterId());
  }

  @Test
  void managesMedicineInventoryAndReducesStockOnBilling() {
    var created =
        pharmacyService.createMedicine(
            new MedicineRequest(
                "TEST-MED-001",
                "Test Medicine",
                "Test Generic",
                "TABLET",
                "Test Pharma",
                "BATCH-01",
                "Integration test medicine",
                null,
                20,
                new BigDecimal("100.00"),
                new BigDecimal("5.00"),
                LocalDate.now().plusYears(1),
                true));

    var updated =
        pharmacyService.updateMedicine(
            created.id(),
            new MedicineRequest(
                "TEST-MED-001",
                "Updated Test Medicine",
                "Test Generic",
                "TABLET",
                "Test Pharma",
                "BATCH-02",
                "Updated description",
                null,
                25,
                new BigDecimal("100.00"),
                new BigDecimal("5.00"),
                LocalDate.now().plusYears(2),
                true));

    var invoice =
        pharmacyService.createSale(
            new CreatePharmacySaleRequest(
                null,
                "Walk-in Customer",
                "9876543210",
                List.of(new CreatePharmacySaleRequest.Item(updated.id(), 3)),
                new BigDecimal("15.00"),
                PaymentMode.CASH));

    assertNotNull(invoice.invoiceNumber());
    assertEquals("Updated Test Medicine", invoice.items().get(0).medicineName());
    assertEquals(new BigDecimal("300.00"), invoice.subtotal());
    assertEquals(new BigDecimal("15.00"), invoice.taxAmount());
    assertEquals(new BigDecimal("300.00"), invoice.totalPayable());
    assertEquals(22, pharmacyService.getMedicine(updated.id()).quantity());

    pharmacyService.deleteMedicine(updated.id());
    assertTrue(
        pharmacyService.searchMedicines("TEST-MED-001", null).stream()
            .noneMatch(medicine -> medicine.id().equals(updated.id())));
  }

  @Test
  void generatesUniqueBloodAndUrineSpecimenBarcodesForLabOrder() {
    var bloodTest =
        labTestService.create(
            new LabTestRequest(
                "BAR-BLOOD",
                "Barcode Blood Test",
                "Haematology",
                new BigDecimal("200"),
                4,
                SpecimenType.BLOOD,
                true,
                List.of()));
    var urineTest =
        labTestService.create(
            new LabTestRequest(
                "BAR-URINE",
                "Barcode Urine Test",
                "Clinical Pathology",
                new BigDecimal("150"),
                4,
                SpecimenType.URINE,
                true,
                List.of()));
    var patient = new Patient();
    patient.setName("Barcode Test Patient");
    patient.setMobile("9876501234");
    patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
    patient.setGender(Gender.MALE);
    patient = patientRepository.save(patient);

    var order =
        labOrderService.create(
            new CreateLabOrderRequest(
                patient.getId(),
                null,
                LabPriority.ROUTINE,
                List.of(bloodTest.id(), urineTest.id()),
                PaymentMode.CASH));

    assertEquals(2, order.specimens().size());
    assertEquals(2, order.specimens().stream().map(label -> label.barcode()).distinct().count());
    assertTrue(
        order.specimens().stream()
            .anyMatch(
                label ->
                    label.specimenType() == SpecimenType.BLOOD
                        && label.testCodes().contains("BAR-BLOOD")));
    assertTrue(
        order.specimens().stream()
            .anyMatch(
                label ->
                    label.specimenType() == SpecimenType.URINE
                        && label.testCodes().contains("BAR-URINE")));
  }
}
