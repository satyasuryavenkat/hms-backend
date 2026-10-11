package com.app.hms.service;

import com.app.hms.common.*;
import com.app.hms.common.Enums.*;
import com.app.hms.common.PageUtils;
import com.app.hms.common.ReferenceNumberGenerator;
import com.app.hms.dao.*;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.entity.*;
import com.app.hms.mapper.IpdMapper;
import com.app.hms.repository.IpdChargeCatalogRepository;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IpdServiceImpl implements IpdService {
  private final IpdDao dao;
  private final PatientService patients;
  private final DoctorService doctors;
  private final UserDao users;
  private final IpdMapper mapper;
  private final PdfService pdf;
  private final ReferenceNumberGenerator referenceNumbers;
  private final IpdChargeCatalogRepository catalogItems;
  private final LabTestDao labTests;

  @Override
  @Transactional
  public IpdAdmissionResponse create(IpdAdmissionRequest r) {
    return create(r, CareType.IPD);
  }

  @Override
  @Transactional
  public IpdAdmissionResponse createDaycare(IpdAdmissionRequest r) {
    return create(r, CareType.DAYCARE);
  }

  private IpdAdmissionResponse create(IpdAdmissionRequest r, CareType careType) {
    if (dao.bedOccupied(r.bedNumber(), careType)) {
      throw new BadRequestException(
          careType == CareType.DAYCARE
              ? "Daycare chair or bed is already occupied"
              : "Bed is already occupied");
    }
    IpdAdmission a = new IpdAdmission();
    a.setCareType(careType);
    fill(a, r);
    a = dao.saveAdmission(a);
    a.setIpdNumber(referenceNumbers.yearly(careType == CareType.DAYCARE ? "DC" : "IPD", a.getId()));
    return mapper.admission(dao.saveAdmission(a));
  }

  @Override
  @Transactional
  public IpdAdmissionResponse update(Long id, IpdAdmissionRequest r) {
    IpdAdmission a = active(id, CareType.IPD);
    fill(a, r);
    return mapper.admission(dao.saveAdmission(a));
  }

  @Override
  @Transactional
  public IpdAdmissionResponse updateDates(Long id, AdmissionDatesRequest request) {
    IpdAdmission a = entity(id);
    if (request.admissionDateTime() == null)
      throw new BadRequestException("Admission date is required");
    if (request.dischargeDateTime() != null && request.dischargeDateTime()
        .atZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata")).toLocalDateTime()
        .isBefore(request.admissionDateTime()))
      throw new BadRequestException("Discharge date cannot be before admission date");
    if (a.getStatus() == AdmissionStatus.DISCHARGED && request.dischargeDateTime() == null)
      throw new BadRequestException("A completed admission requires a discharge date");
    a.setAdmissionDateTime(request.admissionDateTime());
    a.setDischargeDateTime(request.dischargeDateTime());
    return mapper.admission(dao.saveAdmission(a));
  }

  @Override
  @Transactional
  public IpdAdmissionResponse updateDaycare(Long id, IpdAdmissionRequest r) {
    IpdAdmission a = active(id, CareType.DAYCARE);
    fill(a, r);
    return mapper.admission(dao.saveAdmission(a));
  }

  private void fill(IpdAdmission a, IpdAdmissionRequest r) {
    a.setPatient(patients.findEntityById(r.patientId()));
    a.setAdmissionType(r.admissionType());
    a.setAdmissionDateTime(LocalDateTime.of(r.admissionDate(), r.admissionTime()));
    a.setConsultant(doctors.findEntityById(r.consultantId()));
    a.setWard(r.ward());
    a.setBedNumber(r.bedNumber());
    a.setProvisionalDiagnosis(r.provisionalDiagnosis());
    a.setAttendantName(r.attendant().name());
    a.setAttendantMobile(r.attendant().mobile());
  }

  @Override
  public List<IpdAdmissionResponse> search(String q, AdmissionStatus s) {
    return search(q, s, CareType.IPD);
  }

  @Override
  public List<IpdAdmissionResponse> searchDaycare(String q, AdmissionStatus s) {
    return search(q, s, CareType.DAYCARE);
  }

  private List<IpdAdmissionResponse> search(String q, AdmissionStatus s, CareType careType) {
    return dao.search(PageUtils.query(q), s, careType).stream().map(mapper::admission).toList();
  }

  @Override
  public IpdAdmissionResponse findById(Long id) {
    return mapper.admission(entity(id, CareType.IPD));
  }

  @Override
  public IpdAdmissionResponse findDaycareById(Long id) {
    return mapper.admission(entity(id, CareType.DAYCARE));
  }

  @Override
  public IpdChargesResponse charges(Long id) {
    entity(id);
    List<IpdChargeResponse> list = dao.charges(id).stream().map(mapper::charge).toList();
    return new IpdChargesResponse(id, list, sumCharges(id));
  }

  @Override
  @Transactional
  public IpdChargeResponse addCharge(Long id, IpdChargeRequest r) {
    IpdCharge c = new IpdCharge();
    c.setAdmission(active(id));
    set(c, r);
    return mapper.charge(dao.saveCharge(c));
  }

  @Override
  @Transactional
  public IpdChargeResponse updateCharge(Long id, Long chargeId, IpdChargeRequest r) {
    active(id);
    IpdCharge c = charge(id, chargeId);
    set(c, r);
    return mapper.charge(dao.saveCharge(c));
  }

  @Override
  @Transactional
  public void deleteCharge(Long id, Long chargeId) {
    active(id);
    dao.deleteCharge(charge(id, chargeId));
  }

  private void set(IpdCharge c, IpdChargeRequest r) {
    if ("LAB_TESTS".equalsIgnoreCase(r.category())) {
      if (r.labTestId() == null) throw new BadRequestException("Select an available lab test");
      LabTest test = labTests.findById(r.labTestId())
          .filter(LabTest::isActive)
          .orElseThrow(() -> new BadRequestException("Select an available lab test"));
      if (test.getPrice() == null || test.getPrice().signum() < 0) {
        throw new BadRequestException("The selected lab test does not have a valid price");
      }
      c.setCategory("LAB_TESTS");
      c.setDepartment(Objects.requireNonNullElse(test.getDepartment(), "Laboratory"));
      c.setServiceCode(test.getCode());
      c.setDescription(test.getName());
      c.setRate(test.getPrice());
      c.setQuantity(BigDecimal.ONE);
      c.setAmount(test.getPrice());
      return;
    }
    if ("PHARMACY".equalsIgnoreCase(r.category())) {
      if (r.rate() == null || r.rate().signum() < 0) {
        throw new BadRequestException("Enter a valid pharmacy price");
      }
      c.setCategory("PHARMACY");
      c.setDepartment("Pharmacy");
      c.setServiceCode(null);
      c.setDescription(r.description().trim());
      c.setRate(r.rate());
      c.setQuantity(r.quantity());
      c.setAmount(r.quantity().multiply(r.rate()));
      return;
    }
    IpdChargeCatalogItem item =
        catalogItems
            .findByCategoryIgnoreCaseAndDepartmentIgnoreCaseAndServiceNameIgnoreCase(
                categoryKey(r.category()), r.department().trim(), r.description().trim())
            .filter(IpdChargeCatalogItem::isActive)
            .orElseThrow(() -> new BadRequestException("Select an active configured IPD service"));
    c.setCategory(item.getCategory());
    c.setDepartment(item.getDepartment());
    c.setServiceCode(item.getServiceCode());
    c.setDescription(item.getServiceName());
    c.setRate(item.getDefaultRate());
    c.setQuantity(r.quantity());
    c.setAmount(r.quantity().multiply(c.getRate()));
  }

  @Override
  public List<?> catalog() {
    Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
    catalogItems.findByActiveTrueOrderByCategoryLabelAscServiceNameAsc().forEach(item -> {
      Map<String, Object> category = grouped.computeIfAbsent(item.getCategory(), key -> {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("category", item.getCategory());
        value.put("label", item.getCategoryLabel());
        value.put("services", new ArrayList<Map<String, Object>>());
        return value;
      });
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> services = (List<Map<String, Object>>) category.get("services");
      services.add(Map.of(
          "name", item.getServiceName(),
          "department", item.getDepartment(),
          "defaultRate", item.getDefaultRate()));
    });
    return new ArrayList<>(grouped.values());
  }

  @Override
  public List<IpdChargeCatalogItemResponse> catalogItems(Boolean active) {
    List<IpdChargeCatalogItem> items = Boolean.TRUE.equals(active)
        ? catalogItems.findByActiveTrueOrderByCategoryLabelAscServiceNameAsc()
        : catalogItems.findAllByOrderByCategoryLabelAscServiceNameAsc();
    return items.stream().map(this::catalogItemResponse).toList();
  }

  @Override
  @Transactional
  public IpdChargeCatalogItemResponse createCatalogItem(IpdChargeCatalogRequest r) {
    rejectDuplicateCatalogItem(r, null);
    IpdChargeCatalogItem item = applyCatalogItem(new IpdChargeCatalogItem(), r);
    item.setServiceCode(nextInternalServiceCode(r));
    return catalogItemResponse(catalogItems.save(item));
  }

  @Override
  @Transactional
  public IpdChargeCatalogItemResponse updateCatalogItem(Long id, IpdChargeCatalogRequest r) {
    IpdChargeCatalogItem item = catalogItem(id);
    rejectDuplicateCatalogItem(r, id);
    return catalogItemResponse(catalogItems.save(applyCatalogItem(item, r)));
  }

  @Override
  @Transactional
  public void deleteCatalogItem(Long id) {
    catalogItems.delete(catalogItem(id));
  }

  private IpdChargeCatalogItem applyCatalogItem(IpdChargeCatalogItem item, IpdChargeCatalogRequest r) {
    item.setCategory(categoryKey(r.category()));
    item.setCategoryLabel(r.category().trim());
    item.setServiceName(r.serviceName().trim());
    item.setDepartment(r.department().trim());
    item.setDefaultRate(r.defaultRate());
    item.setActive(r.active());
    return item;
  }

  private void rejectDuplicateCatalogItem(IpdChargeCatalogRequest r, Long excludedId) {
    String category = categoryKey(r.category());
    String department = r.department().trim();
    String serviceName = r.serviceName().trim();
    boolean exists = excludedId == null
        ? catalogItems.existsByCategoryIgnoreCaseAndDepartmentIgnoreCaseAndServiceNameIgnoreCase(
            category, department, serviceName)
        : catalogItems.existsByCategoryIgnoreCaseAndDepartmentIgnoreCaseAndServiceNameIgnoreCaseAndIdNot(
            category, department, serviceName, excludedId);
    if (exists) {
      throw new BadRequestException(
          "This service already exists in the selected category and department");
    }
  }

  private String categoryKey(String value) {
    return value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
  }

  private String nextInternalServiceCode(IpdChargeCatalogRequest r) {
    String base = (categoryKey(r.category()) + "_" + categoryKey(r.serviceName()))
        .replaceAll("_+", "_");
    if (base.length() > 74) base = base.substring(0, 74);
    String candidate = base;
    int suffix = 2;
    while (catalogItems.existsByServiceCodeIgnoreCase(candidate)) {
      String ending = "_" + suffix++;
      candidate = base.substring(0, Math.min(base.length(), 80 - ending.length())) + ending;
    }
    return candidate;
  }

  private IpdChargeCatalogItem catalogItem(Long id) {
    return catalogItems.findById(id)
        .orElseThrow(() -> new NotFoundException("IPD charge configuration not found"));
  }

  private IpdChargeCatalogItemResponse catalogItemResponse(IpdChargeCatalogItem item) {
    return new IpdChargeCatalogItemResponse(
        item.getId(), item.getServiceName(), item.getCategoryLabel(), item.getDepartment(),
        item.getDefaultRate(), item.isActive());
  }

  @Override
  @Transactional
  public IpdAdvanceResponse addAdvance(Long id, IpdAdvanceRequest r) {
    IpdAdvance a = new IpdAdvance();
    a.setAdmission(active(id));
    a.setAmount(r.amount());
    a.setPaymentMode(r.paymentMode());
    a.setReferenceNumber(r.referenceNumber());
    a.setRemarks(r.remarks());
    a = dao.saveAdvance(a);
    a.setReceiptNumber(referenceNumbers.dated("ADV", a.getId()));
    return mapper.advance(dao.saveAdvance(a));
  }

  @Override
  public List<IpdAdvanceResponse> advances(Long id) {
    entity(id);
    return dao.advances(id).stream().map(mapper::advance).toList();
  }

  @Override
  public FinalBillResponse finalBill(Long id) {
    return bill(entity(id));
  }

  @Override
  @Transactional
  public FinalBillResponse settle(Long id, FinalBillSettlementRequest r) {
    IpdAdmission a = active(id);
    if (r.dischargeDateTime() != null) {
      if (r.dischargeDateTime().atZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata"))
          .toLocalDateTime().isBefore(a.getAdmissionDateTime()))
        throw new BadRequestException("Discharge date cannot be before admission date");
      a.setDischargeDateTime(r.dischargeDateTime());
    }
    BigDecimal balance =
        sumCharges(id).subtract(sumAdvances(id)).subtract(r.discount()).max(BigDecimal.ZERO);
    if (r.paidAmount().compareTo(balance) != 0)
      throw new BadRequestException("paidAmount must equal balance payable");
    BigDecimal hospitalDiscount = Objects.requireNonNullElse(r.hospitalDiscount(), BigDecimal.ZERO);
    BigDecimal laboratoryDiscount = Objects.requireNonNullElse(r.laboratoryDiscount(), BigDecimal.ZERO);
    BigDecimal pharmacyDiscount = Objects.requireNonNullElse(r.pharmacyDiscount(), BigDecimal.ZERO);
    var chargeList = dao.charges(id);
    BigDecimal hospitalGross = BigDecimal.ZERO, laboratoryGross = BigDecimal.ZERO, pharmacyGross = BigDecimal.ZERO;
    for (var charge : chargeList) {
      if ("LAB_TESTS".equals(charge.getCategory())) laboratoryGross = laboratoryGross.add(charge.getAmount());
      else if ("PHARMACY".equals(charge.getCategory())) pharmacyGross = pharmacyGross.add(charge.getAmount());
      else hospitalGross = hospitalGross.add(charge.getAmount());
    }
    if (hospitalDiscount.compareTo(hospitalGross) > 0 || laboratoryDiscount.compareTo(laboratoryGross) > 0
        || pharmacyDiscount.compareTo(pharmacyGross) > 0)
      throw new BadRequestException("Section discount cannot exceed its gross charges");
    if (r.discount().compareTo(hospitalDiscount.add(laboratoryDiscount).add(pharmacyDiscount)) < 0
        || r.discount().compareTo(hospitalGross.add(laboratoryGross).add(pharmacyGross)) > 0)
      throw new BadRequestException("Total discount must include section discounts and cannot exceed gross charges");
    a.setHospitalDiscount(hospitalDiscount);
    a.setLaboratoryDiscount(laboratoryDiscount);
    a.setPharmacyDiscount(pharmacyDiscount);
    a.setDiscount(r.discount());
    a.setDiscountReason(r.discountReason());
    a.setFinalPaymentMode(r.paymentMode());
    a.setFinalPaidAmount(r.paidAmount());
    a.setPaymentReference(r.paymentReference());
    a.setSettledAt(OffsetDateTime.now());
    return bill(dao.saveAdmission(a));
  }

  @Override
  @Transactional
  public IpdAdmissionResponse saveDischarge(Long id, DischargeRequest r) {
    IpdAdmission a = active(id);
    a.setDischargeDateTime(r.dischargeDateTime());
    a.setDischargeType(r.dischargeType());
    a.setFinalDiagnosis(r.finalDiagnosis());
    a.setClinicalSummary(r.clinicalSummary());
    List<Medication> medications = new ArrayList<>();
    if (r.medications() != null) {
      r.medications()
          .forEach(
              m -> {
                Medication medication = new Medication();
                medication.setMedicine(m.medicine());
                medication.setDose(m.dose());
                medication.setFrequency(m.frequency());
                medication.setDuration(m.duration());
                medication.setInstructions(m.instructions());
                medications.add(medication);
              });
    }
    a.setMedications(medications);
    a.setAdvice(r.advice());
    a.setFollowUpDate(r.followUpDate());
    if (r.followUpDoctorId() != null)
      a.setFollowUpDoctor(doctors.findEntityById(r.followUpDoctorId()));
    a.setDischargeDraft(true);
    a.setDischargeDraftSavedAt(OffsetDateTime.now());
    a.setStatus(AdmissionStatus.DISCHARGE_PLANNED);
    return mapper.admission(dao.saveAdmission(a));
  }

  @Override
  @Transactional
  public IpdAdmissionResponse finalizeDischarge(Long id, FinalizeDischargeRequest r) {
    users
        .findById(r.confirmedBy())
        .orElseThrow(() -> new NotFoundException("Confirming user not found"));
    IpdAdmission a = entity(id);
    if (!a.isDischargeDraft()) throw new BadRequestException("Save discharge draft first");
    if (a.getSettledAt() == null && sumCharges(id).subtract(sumAdvances(id)).signum() > 0)
      throw new BadRequestException("Final bill must be settled before discharge");
    a.setStatus(AdmissionStatus.DISCHARGED);
    return mapper.admission(dao.saveAdmission(a));
  }

  @Override
  public byte[] finalBillPdf(Long id) {
    FinalBillResponse b = finalBill(id);
    return pdf.create(
        "IPD Final Invoice",
        Map.of(
            "Invoice",
            b.invoiceNumber(),
            "Gross",
            b.grossCharges(),
            "Balance",
            b.balancePayable()));
  }

  @Override
  public byte[] dischargePdf(Long id) {
    IpdAdmission a = entity(id);
    if (a.getStatus() != AdmissionStatus.DISCHARGED)
      throw new BadRequestException("Discharge is not finalized");
    return pdf.create(
        "Discharge Summary",
        Map.of(
            "IPD",
            a.getIpdNumber(),
            "Patient",
            a.getPatient().getName(),
            "Diagnosis",
            a.getFinalDiagnosis(),
            "Summary",
            a.getClinicalSummary()));
  }

  private IpdAdmission entity(Long id) {
    return dao.findAdmission(id)
        .orElseThrow(() -> new NotFoundException("Admission not found"));
  }

  private IpdAdmission entity(Long id, CareType careType) {
    IpdAdmission admission = entity(id);
    CareType actual = Objects.requireNonNullElse(admission.getCareType(), CareType.IPD);
    if (actual != careType) throw new NotFoundException("Admission not found");
    return admission;
  }

  private IpdCharge charge(Long admissionId, Long chargeId) {
    return dao.findCharge(admissionId, chargeId)
        .orElseThrow(() -> new NotFoundException("Charge not found"));
  }

  private IpdAdmission active(Long id) {
    IpdAdmission a = entity(id);
    if (a.getStatus() == AdmissionStatus.DISCHARGED || a.getStatus() == AdmissionStatus.CANCELLED)
      throw new BadRequestException("Admission is not active");
    return a;
  }

  private IpdAdmission active(Long id, CareType careType) {
    IpdAdmission admission = active(id);
    CareType actual = Objects.requireNonNullElse(admission.getCareType(), CareType.IPD);
    if (actual != careType) throw new NotFoundException("Admission not found");
    return admission;
  }

  private BigDecimal sumCharges(Long id) {
    return dao.charges(id).stream()
        .map(IpdCharge::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal sumAdvances(Long id) {
    return dao.advances(id).stream()
        .map(IpdAdvance::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private FinalBillResponse bill(IpdAdmission a) {
    BigDecimal gross = sumCharges(a.getId()),
        advance = sumAdvances(a.getId()),
        discount = Objects.requireNonNullElse(a.getDiscount(), BigDecimal.ZERO),
        paid = Objects.requireNonNullElse(a.getFinalPaidAmount(), BigDecimal.ZERO),
        balance = gross.subtract(advance).subtract(discount).subtract(paid).max(BigDecimal.ZERO);
    return new FinalBillResponse(
        (Objects.requireNonNullElse(a.getCareType(), CareType.IPD) == CareType.DAYCARE
            ? "INV-DC-%04d"
            : "INV-IPD-%04d").formatted(a.getId()),
        gross,
        advance,
        discount,
        paid,
        balance,
        a.getFinalPaymentMode(),
        balance.signum() == 0 ? PaymentStatus.PAID : PaymentStatus.PENDING,
        a.getSettledAt(),
        dao.charges(a.getId()).stream().map(mapper::charge).toList(),
        Objects.requireNonNullElse(a.getHospitalDiscount(), BigDecimal.ZERO),
        Objects.requireNonNullElse(a.getLaboratoryDiscount(), BigDecimal.ZERO),
        Objects.requireNonNullElse(a.getPharmacyDiscount(), BigDecimal.ZERO));
  }
}
