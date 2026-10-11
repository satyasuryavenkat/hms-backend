package com.app.hms.dto.response;

import java.math.BigDecimal;

public record IpdChargeResponse(
    Long id,
    String category,
    String department,
    String description,
    BigDecimal quantity,
    BigDecimal rate,
    BigDecimal amount) {}
