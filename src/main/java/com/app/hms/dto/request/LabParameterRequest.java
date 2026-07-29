package com.app.hms.dto.request;

public record LabParameterRequest(
    Long parameterId, String name, String unit, String referenceRange) {}
