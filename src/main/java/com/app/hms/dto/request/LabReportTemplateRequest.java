package com.app.hms.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LabReportTemplateRequest(@NotBlank String html) {}
