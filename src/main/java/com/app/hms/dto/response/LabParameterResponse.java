package com.app.hms.dto.response;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record LabParameterResponse(
    @JsonSerialize(using = ToStringSerializer.class) Long parameterId,
    String name,
    String unit,
    String referenceRange) {}
