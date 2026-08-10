package com.app.hms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.hms.common.Enums.Gender;
import com.app.hms.common.Enums.AdmissionType;
import com.app.hms.common.Enums.DischargeType;
import com.app.hms.common.Enums.FollowUpStatus;
import com.app.hms.common.Enums.LabPriority;
import com.app.hms.common.Enums.LabParameterType;
import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.common.Enums.PaymentMode;
import com.app.hms.common.Enums.SpecimenType;
import com.app.hms.common.NotFoundException;
import com.app.hms.dto.request.CreateLabOrderRequest;
import com.app.hms.dto.request.CreatePharmacySaleRequest;
import com.app.hms.dto.request.DoctorRequest;
import com.app.hms.dto.request.AttendantRequest;
import com.app.hms.dto.request.DischargeRequest;
import com.app.hms.dto.request.IpdAdmissionRequest;
import com.app.hms.dto.request.FollowUpRequest;
import com.app.hms.dto.request.LabParameterRequest;
import com.app.hms.dto.request.LabResultRequest;
import com.app.hms.dto.request.LabTestRequest;
import com.app.hms.dto.request.MedicineRequest;
import com.app.hms.dto.request.PublishLabReportRequest;
import com.app.hms.dto.request.PatientRequest;
import com.app.hms.dto.request.SaveLabReportRequest;
import com.app.hms.dao.UserDao;
import com.app.hms.entity.Patient;
import com.app.hms.repository.PatientRepository;
import com.app.hms.repository.FollowUpRepository;
import com.app.hms.service.DoctorService;
import com.app.hms.service.LabOrderService;
import com.app.hms.service.LabTestService;
import com.app.hms.service.IpdService;
import com.app.hms.service.FollowUpService;
import com.app.hms.service.PatientService;
import com.app.hms.service.PharmacyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
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
  @Autowired private DoctorService doctorService;
  @Autowired private LabOrderService labOrderService;
  @Autowired private PharmacyService pharmacyService;
  @Autowired private IpdService ipdService;
  @Autowired private PatientService patientService;
  @Autowired private FollowUpService followUpService;
  @Autowired private FollowUpRepository followUpRepository;
  @Autowired private PatientRepository patientRepository;
  @Autowired private UserDao userDao;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void contextLoads() {}

  @Test
  void showsFollowUpOnlyOneDayBeforeAndKeepsRemindedStatusForToday() {
    var patient =
        patientService.create(
            new PatientRequest(
                "Follow-up Test Patient",
                "9876509876",
                LocalDate.of(1992, 6, 15),
                Gender.FEMALE,
                "Test address"));
    var tomorrowReminder =
        followUpService.create(
            new FollowUpRequest(patient.id(), LocalDate.now().plusDays(1), LocalTime.of(10, 30)),
            "reception@test");
    followUpService.create(
        new FollowUpRequest(patient.id(), LocalDate.now().plusDays(2), LocalTime.of(11, 0)),
        "reception@test");

    followUpService.refreshReminderWindow();
    var reminders = followUpService.reminders();

    assertEquals(1, reminders.size());
    assertEquals(tomorrowReminder.id(), reminders.get(0).id());
    assertEquals(FollowUpStatus.PENDING, reminders.get(0).status());

    var marked = followUpService.markReminded(tomorrowReminder.id(), "reception@test");
    assertEquals(FollowUpStatus.REMINDED, marked.status());
    assertNotNull(marked.remindedAt());
    assertEquals(1, followUpService.reminders().size());
    assertTrue(followUpRepository.findById(tomorrowReminder.id()).orElseThrow().isReminderVisible());
  }

  @Test
  void savesDischargeDraftWithEmptyMutableMedicationCollection() {
    var patient =
        patientService.create(
            new PatientRequest(
                "Discharge Test Patient",
                "9876504321",
                LocalDate.of(1985, 1, 1),
                Gender.FEMALE,
                "Test address"));
    var doctor =
        doctorService.create(
            new DoctorRequest(
                "DOC-DISCHARGE-TEST",
                "Dr. Discharge Test",
                "General Medicine",
                "Physician",
                new BigDecimal("500"),
                true));
    var admission =
        ipdService.create(
            new IpdAdmissionRequest(
                patient.id(),
                AdmissionType.PLANNED,
                LocalDate.now(),
                LocalTime.now(),
                doctor.id(),
                "General Ward",
                "TEST-DISCHARGE-BED",
                "Observation",
                new AttendantRequest("Test Attendant", "9876504322")));

    var saved =
        ipdService.saveDischarge(
            admission.id(),
            new DischargeRequest(
                OffsetDateTime.now(),
                DischargeType.ROUTINE,
                "Recovered",
                "Patient is clinically stable",
                List.of(),
                "Follow prescribed advice",
                null,
                doctor.id()));

    assertTrue(saved.dischargeDraft());
    assertTrue(saved.medications().isEmpty());
  }

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
                List.of(
                    new LabParameterRequest(
                        null, "Haemoglobin", "g/dL", "12 - 16", LabParameterType.NUMERIC))));

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
                        "11 - 17",
                        LabParameterType.NUMERIC),
                    new LabParameterRequest(
                        null, "WBC", "cells/cumm", "4000 - 11000", LabParameterType.NUMERIC))));

    assertEquals("Updated Test Profile", updated.name());
    assertEquals(new BigDecimal("650"), updated.price());
    assertEquals(SpecimenType.BLOOD, updated.specimenType());
    assertEquals(2, updated.parameters().size());
    assertNotNull(updated.parameters().get(1).parameterId());

    var templated =
        labTestService.updateReportTemplate(
            created.id(), "<h2>ADMIN QUILL TEMPLATE</h2><table><tbody><tr><td>Result</td></tr></tbody></table>");
    assertTrue(templated.reportTemplateHtml().contains("ADMIN QUILL TEMPLATE"));

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
  void searchesActiveAndInactiveLabTestsWhenActiveFilterIsMissing() {
    var activeTest =
        labTestService.create(
            new LabTestRequest(
                "LAB-SEARCH-ACTIVE",
                "Active Search Test",
                "Biochemistry",
                new BigDecimal("100"),
                2,
                SpecimenType.BLOOD,
                true,
                List.of()));
    var inactiveTest =
        labTestService.create(
            new LabTestRequest(
                "LAB-SEARCH-INACTIVE",
                "Inactive Search Test",
                "Biochemistry",
                new BigDecimal("100"),
                2,
                SpecimenType.BLOOD,
                false,
                List.of()));

    var allMatches = labTestService.search("Search Test", null, null);
    var activeMatches = labTestService.search("Search Test", null, true);

    assertTrue(allMatches.stream().anyMatch(test -> test.id().equals(activeTest.id())));
    assertTrue(allMatches.stream().anyMatch(test -> test.id().equals(inactiveTest.id())));
    assertTrue(activeMatches.stream().anyMatch(test -> test.id().equals(activeTest.id())));
    assertTrue(activeMatches.stream().noneMatch(test -> test.id().equals(inactiveTest.id())));
  }

  @Test
  void deletesUnusedDoctorsAndLabTests() {
    var doctor =
        doctorService.create(
            new DoctorRequest(
                "DOC-DELETE-TEST",
                "Dr. Delete Test",
                "Test Department",
                "Test Specialty",
                new BigDecimal("500"),
                true));
    var labTest =
        labTestService.create(
            new LabTestRequest(
                "LAB-DELETE-TEST",
                "Delete Test",
                "Test Department",
                new BigDecimal("250"),
                4,
                SpecimenType.BLOOD,
                true,
                List.of()));

    doctorService.delete(doctor.id());
    labTestService.delete(labTest.id());

    assertThrows(NotFoundException.class, () -> doctorService.findResponseById(doctor.id()));
    assertThrows(NotFoundException.class, () -> labTestService.findById(labTest.id()));
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
                List.of(
                    new LabParameterRequest(
                        null, "CELL COUNTS", "", "", LabParameterType.HEADING),
                    new LabParameterRequest(
                        null, "Cell Count", "cells/cumm", "4000 - 11000", LabParameterType.NUMERIC),
                    new LabParameterRequest(
                        null, "Morphology", "", "", LabParameterType.TEXT))));
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

    var pendingOrders =
        labOrderService.search("", LabReportStatus.PENDING, null, 0, 20).content();
    assertTrue(pendingOrders.stream().anyMatch(pending -> pending.id().equals(order.id())));

    var reportItems = order.tests().stream().flatMap(test -> test.results().stream()).toList();
    assertTrue(
        reportItems.stream()
            .anyMatch(item -> item.parameterType() == LabParameterType.HEADING));
    var saved =
        labOrderService.saveReport(
            order.id(),
            new SaveLabReportRequest(
                reportItems.stream()
                    .filter(item -> item.parameterType() != LabParameterType.HEADING)
                    .map(item -> new LabResultRequest(item.parameterId(), "Normal", "", false))
                    .toList(),
                "No significant abnormality"));
    var published =
        labOrderService.publish(
            saved.id(),
            new PublishLabReportRequest(
                userDao.findAll().get(0).getId(), "No significant abnormality"));
    assertEquals(LabReportStatus.VERIFIED, published.reportStatus());
  }
}
