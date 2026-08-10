package com.app.hms.service;

import com.app.hms.common.*;
import com.app.hms.common.Enums.*;
import com.app.hms.common.PageUtils;
import com.app.hms.common.ReferenceNumberGenerator;
import com.app.hms.dao.*;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.entity.*;
import com.app.hms.mapper.LabOrderMapper;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabOrderServiceImpl implements LabOrderService {
  private final LabOrderDao orders;
  private final LabTestDao tests;
  private final PatientService patients;
  private final DoctorService doctors;
  private final UserDao users;
  private final LabOrderMapper mapper;
  private final PdfService pdf;
  private final ReferenceNumberGenerator referenceNumbers;

  @Override
  @Transactional
  public LabOrderResponse create(CreateLabOrderRequest r) {
    LabOrder o = new LabOrder();
    o.setPatient(patients.findEntityById(r.patientId()));
    if (r.referringDoctorId() != null)
      o.setReferringDoctor(doctors.findEntityById(r.referringDoctorId()));
    o.setPriority(r.priority());
    BigDecimal total = BigDecimal.ZERO;
    for (Long id : new LinkedHashSet<>(r.testIds())) {
      LabTest t =
          tests
              .findById(id)
              .filter(LabTest::isActive)
              .orElseThrow(() -> new BadRequestException("Invalid or inactive lab test: " + id));
      LabOrderItem i = new LabOrderItem();
      i.setOrder(o);
      i.setTestId(t.getId());
      i.setCode(t.getCode());
      i.setName(t.getName());
      i.setDepartment(t.getDepartment());
      i.setSpecimenType(t.getSpecimenType() == null ? SpecimenType.BLOOD : t.getSpecimenType());
      i.setAmount(t.getPrice());
      i.setReportTemplateHtml(t.getReportTemplateHtml());
      addReportParameters(i, t);
      o.getTests().add(i);
      total = total.add(t.getPrice());
    }
    o.setSubtotal(total);
    o.setTotalPayable(total);
    o.setPaidAmount(total);
    o.setPaymentMode(r.paymentMode());
    o.setPaymentStatus(PaymentStatus.PAID);
    o = orders.save(o);
    o.setOrderNumber(referenceNumbers.dated("LAB", o.getId()));
    ensureSpecimens(o);
    return mapper.toResponse(orders.save(o));
  }

  @Override
  @Transactional
  public PageResponse<LabOrderResponse> search(
      String q, LabReportStatus s, LocalDate d, int page, int size) {
    Page<LabOrder> p = orders.search(PageUtils.query(q), s, d, PageUtils.request(page, size));
    p.getContent()
        .forEach(
            order -> {
              ensureReportParameters(order);
              ensureSpecimens(order);
            });
    return new PageResponse<>(
        p.stream().map(mapper::toResponse).toList(),
        p.getNumber(),
        p.getSize(),
        p.getTotalElements(),
        p.getTotalPages());
  }

  @Override
  @Transactional
  public LabOrderResponse findById(Long id) {
    LabOrder order = entity(id);
    ensureReportParameters(order);
    ensureSpecimens(order);
    return mapper.toResponse(order);
  }

  @Override
  @Transactional
  public LabOrderResponse cancel(Long id, CancelLabOrderRequest r) {
    LabOrder o = entity(id);
    if (o.getReportStatus() == LabReportStatus.VERIFIED
        || o.getReportStatus() == LabReportStatus.PUBLISHED)
      throw new BadRequestException("Published orders cannot be cancelled");
    o.setReportStatus(LabReportStatus.CANCELLED);
    o.setCancellationReason(r.reason());
    return mapper.toResponse(orders.save(o));
  }

  @Override
  @Transactional
  public LabOrderResponse saveReport(Long id, SaveLabReportRequest r) {
    LabOrder o = entity(id);
    if (o.getReportStatus() == LabReportStatus.CANCELLED)
      throw new BadRequestException("Cancelled order cannot be reported");
    Map<Long, LabResult> all = new HashMap<>();
    o.getTests().forEach(i -> i.getResults().forEach(x -> all.put(x.getParameterId(), x)));
    for (LabResultRequest x : r.results()) {
      LabResult result =
          Optional.ofNullable(all.get(x.parameterId()))
              .orElseThrow(() -> new BadRequestException("Unknown parameter: " + x.parameterId()));
      if (result.getParameterType() == LabParameterType.HEADING) continue;
      result.setResult(x.result());
      result.setRemarks(x.remarks());
      result.setAbnormal(x.abnormal());
    }
    o.setRemarks(r.remarks());
    o.setReportStatus(LabReportStatus.DRAFT);
    return mapper.toResponse(orders.save(o));
  }

  @Override
  @Transactional
  public LabOrderResponse publish(Long id, PublishLabReportRequest r) {
    LabOrder o = entity(id);
    AppUser u =
        users
            .findById(r.pathologistId())
            .orElseThrow(() -> new NotFoundException("Pathologist not found"));
    boolean missing =
        o.getTests().stream()
            .flatMap(i -> i.getResults().stream())
            .filter(x -> x.getParameterType() != LabParameterType.HEADING)
            .anyMatch(x -> x.getResult() == null || x.getResult().isBlank());
    if (missing) throw new BadRequestException("All report parameters require results");
    o.setReportStatus(LabReportStatus.VERIFIED);
    o.setVerifiedBy(u.getId());
    o.setVerifiedAt(OffsetDateTime.now());
    o.setRemarks(r.remarks());
    return mapper.toResponse(orders.save(o));
  }

  @Override
  public byte[] receiptPdf(Long id) {
    LabOrder o = entity(id);
    return pdf.create(
        "Laboratory Receipt",
        Map.of(
            "Order",
            o.getOrderNumber(),
            "Patient",
            o.getPatient().getName(),
            "Amount",
            o.getTotalPayable()));
  }

  @Override
  public byte[] reportPdf(Long id) {
    LabOrder o = entity(id);
    if (o.getReportStatus() != LabReportStatus.VERIFIED
        && o.getReportStatus() != LabReportStatus.PUBLISHED)
      throw new BadRequestException("Report is not published");
    return pdf.create(
        "Laboratory Report",
        Map.of(
            "Order",
            o.getOrderNumber(),
            "Patient",
            o.getPatient().getName(),
            "Status",
            o.getReportStatus(),
            "Remarks",
            Objects.toString(o.getRemarks(), "")));
  }

  private LabOrder entity(Long id) {
    return orders.findById(id).orElseThrow(() -> new NotFoundException("Lab order not found"));
  }

  private void ensureReportParameters(LabOrder order) {
    for (LabOrderItem item : order.getTests()) {
      LabTest test = tests.findById(item.getTestId()).orElse(null);
      if (!item.getResults().isEmpty()) {
        if (test == null || test.getParameters().isEmpty()) {
          continue;
        }
        if (isUnusedGeneralResult(item)
            || (isBlankReport(item) && !matchesCurrentTemplate(item, test))) {
          item.getResults().clear();
        } else {
          continue;
        }
      }
      addReportParameters(item, test);
    }
  }

  private void ensureSpecimens(LabOrder order) {
    Set<SpecimenType> required = new LinkedHashSet<>();
    for (LabOrderItem item : order.getTests()) {
      if (item.getSpecimenType() == null) {
        LabTest test = tests.findById(item.getTestId()).orElse(null);
        item.setSpecimenType(
            test == null || test.getSpecimenType() == null
                ? SpecimenType.BLOOD
                : test.getSpecimenType());
      }
      required.add(item.getSpecimenType());
    }
    Set<SpecimenType> existing =
        order.getSpecimens().stream()
            .map(LabSpecimen::getSpecimenType)
            .collect(java.util.stream.Collectors.toSet());
    for (SpecimenType type : required) {
      if (existing.contains(type)) continue;
      LabSpecimen specimen = new LabSpecimen();
      specimen.setOrder(order);
      specimen.setSpecimenType(type);
      specimen.setBarcode(referenceNumbers.dated(specimenPrefix(type), order.getId()));
      order.getSpecimens().add(specimen);
    }
  }

  private String specimenPrefix(SpecimenType type) {
    return switch (type) {
      case BLOOD -> "SBL";
      case URINE -> "SUR";
      case STOOL -> "SST";
      case SWAB -> "SSW";
      case SPUTUM -> "SSP";
      case OTHER -> "SOT";
    };
  }

  private boolean isUnusedGeneralResult(LabOrderItem item) {
    if (item.getResults().size() != 1) return false;
    LabResult result = item.getResults().get(0);
    return (result.getResult() == null || result.getResult().isBlank())
        && Objects.equals(result.getName(), item.getName() + " Result")
        && Objects.equals(result.getReferenceRange(), "As per clinical interpretation");
  }

  private boolean isBlankReport(LabOrderItem item) {
    return item.getResults().stream()
        .allMatch(
            result ->
                (result.getResult() == null || result.getResult().isBlank())
                    && (result.getRemarks() == null || result.getRemarks().isBlank()));
  }

  private boolean matchesCurrentTemplate(LabOrderItem item, LabTest test) {
    Set<Long> resultIds =
        item.getResults().stream().map(LabResult::getParameterId).collect(java.util.stream.Collectors.toSet());
    Set<Long> templateIds =
        test.getParameters().stream()
            .map(LabParameter::getParameterId)
            .collect(java.util.stream.Collectors.toSet());
    return resultIds.equals(templateIds);
  }

  private void addReportParameters(LabOrderItem item, LabTest test) {
    if (test != null && !test.getParameters().isEmpty()) {
      List<LabParameter> parameters =
          test.getParameters().stream()
              .sorted(
                  Comparator.comparing(
                      parameter ->
                          parameter.getDisplayOrder() == null
                              ? Integer.MAX_VALUE
                              : parameter.getDisplayOrder()))
              .toList();
      for (int index = 0; index < parameters.size(); index++) {
        LabParameter parameter = parameters.get(index);
        LabResult result = new LabResult();
        result.setItem(item);
        result.setParameterId(
            parameter.getParameterId() == null
                ? UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE
                : parameter.getParameterId());
        result.setName(parameter.getName());
        result.setUnit(parameter.getUnit());
        result.setReferenceRange(parameter.getReferenceRange());
        result.setParameterType(
            parameter.getParameterType() == null
                ? LabParameterType.NUMERIC
                : parameter.getParameterType());
        result.setDisplayOrder(
            parameter.getDisplayOrder() == null ? index : parameter.getDisplayOrder());
        item.getResults().add(result);
      }
      return;
    }
    LabResult result = new LabResult();
    result.setItem(item);
    result.setParameterId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
    result.setName(item.getName() + " Result");
    result.setUnit("");
    result.setReferenceRange("As per clinical interpretation");
    result.setParameterType(LabParameterType.TEXT);
    result.setDisplayOrder(0);
    item.getResults().add(result);
  }
}
