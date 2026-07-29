package com.app.hms.dto.request;

import jakarta.validation.constraints.NotNull;

public record LabResultRequest(
    @NotNull Long parameterId, String result, String remarks, boolean abnormal) {}
