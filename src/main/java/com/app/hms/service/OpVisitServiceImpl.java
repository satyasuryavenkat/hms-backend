package com.app.hms.service;

import com.app.hms.common.*;
import com.app.hms.common.Enums.PaymentStatus;
import com.app.hms.common.PageUtils;
import com.app.hms.common.ReferenceNumberGenerator;
import com.app.hms.dao.OpVisitDao;
import com.app.hms.dto.request.OpVisitRequest;
import com.app.hms.dto.response.*;
import com.app.hms.entity.OpVisit;
import com.app.hms.mapper.OpVisitMapper;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpVisitServiceImpl implements OpVisitService {
  private final OpVisitDao dao;
  private final PatientService patients;
  private final DoctorService doctors;
  private final OpVisitMapper mapper;
  private final PdfService pdfService;
  private final ReferenceNumberGenerator referenceNumbers;

  @Override
  @Transactional
  public OpVisitResponse create(OpVisitRequest r) {
    OpVisit v = new OpVisit();
    fill(v, r);
    v = dao.save(v);
    v.setOpNumber(referenceNumbers.dated("OP", v.getId()));
    v.setReceiptNumber(referenceNumbers.dated("OPR", v.getId()));
    return mapper.toResponse(dao.save(v));
  }

  @Override
  @Transactional
  public OpVisitResponse update(Long id, OpVisitRequest r) {
    OpVisit v = entity(id);
    fill(v, r);
    return mapper.toResponse(dao.save(v));
  }

  private void fill(OpVisit v, OpVisitRequest r) {
    if (r.patientId() == null && r.patient() == null)
      throw new BadRequestException("patientId or patient is required");
    v.setPatient(
        r.patientId() != null
            ? patients.findEntityById(r.patientId())
            : patients.createEntity(r.patient()));
    v.setDoctor(doctors.findEntityById(r.doctorId()));
    v.setVisitType(r.visitType());
    v.setAppointmentDate(r.appointmentDate());
    v.setAppointmentTime(r.appointmentTime());
    v.setSymptoms(r.symptoms());
    v.setConsultationFee(r.consultationFee());
    v.setDiscount(r.discount());
    var total = r.consultationFee().subtract(r.discount());
    if (total.signum() < 0) throw new BadRequestException("Discount cannot exceed fee");
    v.setTotalPayable(total);
    v.setPaidAmount(total);
    v.setPaymentMode(r.paymentMode());
    v.setPaymentStatus(PaymentStatus.PAID);
  }

  @Override
  public OpVisitResponse findById(Long id) {
    return mapper.toResponse(entity(id));
  }

  @Override
  public OpVisitPageResponse search(String q, LocalDate date, int page, int size) {
    Page<OpVisit> p = dao.search(PageUtils.query(q), date, PageUtils.request(page, size));
    return new OpVisitPageResponse(
        p.stream().map(mapper::toResponse).toList(),
        p.getNumber(),
        p.getSize(),
        p.getTotalElements(),
        p.getTotalPages());
  }

  @Override
  public byte[] generateReceiptPdf(Long id) {
    OpVisit v = entity(id);
    return pdfService.create(
        "OP Receipt",
        Map.of(
            "Receipt",
            v.getReceiptNumber(),
            "Patient",
            v.getPatient().getName(),
            "Doctor",
            v.getDoctor().getName(),
            "Amount",
            v.getTotalPayable()));
  }

  private OpVisit entity(Long id) {
    return dao.findById(id).orElseThrow(() -> new NotFoundException("OP visit not found"));
  }
}
