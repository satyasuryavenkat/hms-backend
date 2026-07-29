package com.app.hms.mapper;

import com.app.hms.common.Enums.SpecimenType;
import com.app.hms.dto.request.LabTestRequest;
import com.app.hms.dto.response.LabParameterResponse;
import com.app.hms.dto.response.LabTestResponse;
import com.app.hms.entity.LabParameter;
import com.app.hms.entity.LabTest;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LabTestMapper {
  public LabTestResponse toResponse(LabTest t) {
    return new LabTestResponse(
        t.getId(),
        t.getCode(),
        t.getName(),
        t.getDepartment(),
        t.getPrice(),
        t.getTurnaroundHours(),
        t.getSpecimenType() == null ? SpecimenType.BLOOD : t.getSpecimenType(),
        t.isActive(),
        t.getParameters().stream()
            .map(
                p ->
                    new LabParameterResponse(
                        p.getParameterId(), p.getName(), p.getUnit(), p.getReferenceRange()))
            .toList());
  }

  public void update(LabTest t, LabTestRequest r) {
    t.setCode(r.code());
    t.setName(r.name());
    t.setDepartment(r.department());
    t.setPrice(r.price());
    t.setTurnaroundHours(r.turnaroundHours());
    t.setSpecimenType(r.specimenType());
    t.setActive(r.active());
    if (r.parameters() != null) {
      // Keep Hibernate's managed collection instance. Replacing an @ElementCollection
      // with Stream.toList() creates an immutable collection and breaks both flush and
      // subsequent updates.
      if (t.getParameters() == null) {
        t.setParameters(new ArrayList<>());
      } else {
        t.getParameters().clear();
      }
      r.parameters()
          .forEach(
              p -> {
                LabParameter entity = new LabParameter();
                entity.setParameterId(
                    p.parameterId() == null
                        ? UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE
                        : p.parameterId());
                entity.setName(p.name());
                entity.setUnit(p.unit());
                entity.setReferenceRange(p.referenceRange());
                t.getParameters().add(entity);
              });
    }
  }
}
