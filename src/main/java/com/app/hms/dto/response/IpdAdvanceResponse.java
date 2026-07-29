package com.app.hms.dto.response;

import com.app.hms.common.Enums.PaymentMode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record IpdAdvanceResponse(
    Long id,
    String receiptNumber,
    BigDecimal amount,
    PaymentMode paymentMode,
    String referenceNumber,
    String remarks,
    OffsetDateTime createdAt) {}
