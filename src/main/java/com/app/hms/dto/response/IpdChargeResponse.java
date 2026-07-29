package com.app.hms.dto.response;

import java.math.BigDecimal;

public record IpdChargeResponse(
    Long id,
    String category,
    String department,
    String serviceCode,
    String description,
    BigDecimal quantity,
    BigDecimal rate,
    BigDecimal amount) {}
