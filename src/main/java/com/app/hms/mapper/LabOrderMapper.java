package com.app.hms.mapper;

import com.app.hms.common.Enums.LabParameterType;
import com.app.hms.dto.response.LabOrderResponse;
import com.app.hms.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LabOrderMapper {
  private final PatientMapper patients;
  private final DoctorMapper doctors;

  public LabOrderResponse toResponse(LabOrder o) {
    var items =
        o.getTests().stream()
            .map(
                i ->
                    new LabOrderResponse.TestItem(
                        i.getTestId(),
                        i.getCode(),
                        i.getName(),
                        i.getDepartment(),
                        i.getSpecimenType(),
                        i.getAmount(),
                        i.getStatus(),
                        i.getResults().stream()
                            .sorted(
                                java.util.Comparator.comparing(
                                    r ->
                                        r.getDisplayOrder() == null
                                            ? Integer.MAX_VALUE
                                            : r.getDisplayOrder()))
                            .map(
                                r ->
                                    new LabOrderResponse.ResultItem(
                                        r.getParameterId(),
                                        r.getName(),
                                        r.getResult(),
                                        r.getUnit(),
                                        r.getReferenceRange(),
                                        r.getRemarks(),
                                        r.isAbnormal(),
                                        r.getParameterType() == null
                                            ? LabParameterType.NUMERIC
                                            : r.getParameterType(),
                                        r.getDisplayOrder()))
                            .toList(),
                        i.getReportTemplateHtml()))
            .toList();
    var specimens =
        o.getSpecimens().stream()
            .map(
                specimen ->
                    new LabOrderResponse.SpecimenLabel(
                        specimen.getBarcode(),
                        specimen.getSpecimenType(),
                        o.getTests().stream()
                            .filter(i -> i.getSpecimenType() == specimen.getSpecimenType())
                            .map(LabOrderItem::getCode)
                            .toList(),
                        specimen.getCreatedAt()))
            .toList();
    return new LabOrderResponse(
        o.getId(),
        o.getOrderNumber(),
        patients.toResponse(o.getPatient()),
        o.getReferringDoctor() == null ? null : doctors.toResponse(o.getReferringDoctor()),
        o.getPriority(),
        items,
        specimens,
        o.getSubtotal(),
        o.getDiscount(),
        o.getTotalPayable(),
        o.getPaidAmount(),
        o.getPaymentMode(),
        o.getPaymentStatus(),
        o.getReportStatus(),
        o.getRemarks(),
        o.getCreatedAt());
  }
}
