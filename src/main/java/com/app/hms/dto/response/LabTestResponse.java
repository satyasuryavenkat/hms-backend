package com.app.hms.dto.response;

import com.app.hms.common.Enums.SpecimenType;
import java.math.BigDecimal;
import java.util.List;

public record LabTestResponse(
    Long id,
    String code,
    String name,
    String department,
    BigDecimal price,
    Integer turnaroundHours,
    SpecimenType specimenType,
    boolean active,
    List<LabParameterResponse> parameters,
    String reportTemplateHtml) {}
