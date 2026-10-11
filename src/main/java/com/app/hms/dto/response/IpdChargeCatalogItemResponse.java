package com.app.hms.dto.response;

import java.math.BigDecimal;

public record IpdChargeCatalogItemResponse(
    Long id,
    String serviceName,
    String category,
    String department,
    BigDecimal defaultRate,
    boolean active) {}
