package com.app.hms.dto.request;

import com.app.hms.common.Enums.LabSignatureDesignation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SaveLabReportRequest(
    @NotEmpty(message = "Add at least one report result before saving")
        List<@Valid LabResultRequest> results,
    String remarks,
    LabSignatureDesignation signatureDesignation) {}
