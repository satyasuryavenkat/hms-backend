package com.app.hms.dto.response;

import java.util.List;

public record PatientSearchResponse(
    List<PatientResponse> content, int page, int size, long totalElements, int totalPages) {}
