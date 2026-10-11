package com.app.hms.dto.request;

import com.app.hms.common.Enums.LabSignatureDesignation;
import jakarta.validation.constraints.NotNull;

public record PublishLabReportRequest(
    @NotNull Long pathologistId,
    String remarks,
    LabSignatureDesignation signatureDesignation) {}
