package com.app.hms.dto.request;

import com.app.hms.common.Enums.PaymentMode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record IpdAdvanceRequest(
    @NotNull @Positive BigDecimal amount,
    @NotNull PaymentMode paymentMode,
    String referenceNumber,
    String remarks) {}
