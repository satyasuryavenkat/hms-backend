package com.app.hms.dto.request;

import com.app.hms.common.Enums.LabParameterType;

public record LabParameterRequest(
    Long parameterId,
    String name,
    String unit,
    String referenceRange,
    LabParameterType parameterType) {}
