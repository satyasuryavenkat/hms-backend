package com.app.hms.dto.request;

import jakarta.validation.constraints.NotNull;

public record PublishLabReportRequest(@NotNull Long pathologistId, String remarks) {}
