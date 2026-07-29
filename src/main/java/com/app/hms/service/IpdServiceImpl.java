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

  @Override
  @Transactional
  public IpdAdmissionResponse create(IpdAdmissionRequest r) {
    if (dao.bedOccupied(r.bedNumber())) throw new BadRequestException("Bed is already occupied");
    IpdAdmission a = new IpdAdmission();
    fill(a, r);
    a = dao.saveAdmission(a);
    a.setIpdNumber(referenceNumbers.yearly("IPD", a.getId()));
    return mapper.admission(dao.saveAdmission(a));
  }

  @Override
  @Transactional
  public IpdAdmissionResponse update(Long id, IpdAdmissionRequest r) {
    IpdAdmission a = active(id);
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
    return dao.search(PageUtils.query(q), s).stream().map(mapper::admission).toList();
  }

  @Override
  public IpdAdmissionResponse findById(Long id) {
    return mapper.admission(entity(id));
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
    c.setCategory(r.category());
    c.setDepartment(r.department());
    c.setServiceCode(r.serviceCode());
    c.setDescription(r.description());
    c.setQuantity(r.quantity());
    c.setRate(r.rate());
    c.setAmount(r.quantity().multiply(r.rate()));
  }

  @Override
  public List<?> catalog() {
    return List.of(
        Map.of(
            "category",
            "ROOM_CHARGES",
            "label",
            "Room Charges",
            "services",
            List.of(
                Map.of(
                    "code",
                    "ROOM-GENERAL",
                    "name",
                    "General ward stay",
                    "department",
                    "Inpatient Services",
                    "defaultRate",
                    1000),
                Map.of(
                    "code",
                    "ROOM-DELUXE",
                    "name",
                    "Deluxe room stay",
                    "department",
                    "Inpatient Services",
                    "defaultRate",
                    3200))),
        Map.of("category", "INVESTIGATIONS", "label", "Investigations", "services", List.of()));
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
    BigDecimal balance =
        sumCharges(id).subtract(sumAdvances(id)).subtract(r.discount()).max(BigDecimal.ZERO);
    if (r.paidAmount().compareTo(balance) != 0)
      throw new BadRequestException("paidAmount must equal balance payable");
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
    a.setMedications(
        r.medications() == null
            ? new ArrayList<>()
            : r.medications().stream()
                .map(
                    m -> {
                      Medication medication = new Medication();
                      medication.setMedicine(m.medicine());
                      medication.setDose(m.dose());
                      medication.setFrequency(m.frequency());
                      medication.setDuration(m.duration());
                      medication.setInstructions(m.instructions());
                      return medication;
                    })
                .toList());
    a.setAdvice(r.advice());
    a.setFollowUpDate(r.followUpDate());
    if (r.followUpDoctorId() != null)
      a.setFollowUpDoctor(doctors.findEntityById(r.followUpDoctorId()));
    a.setDischargeDraft(true);
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
        .orElseThrow(() -> new NotFoundException("IPD admission not found"));
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
        "INV-IPD-%04d".formatted(a.getId()),
        gross,
        advance,
        discount,
        paid,
        balance,
        a.getFinalPaymentMode(),
        balance.signum() == 0 ? PaymentStatus.PAID : PaymentStatus.PENDING,
        a.getSettledAt(),
        dao.charges(a.getId()).stream().map(mapper::charge).toList());
  }
}
