package com.app.hms.dto.response;

import java.util.List;

public record OpVisitPageResponse(
    List<OpVisitResponse> content, int page, int size, long totalElements, int totalPages) {}
