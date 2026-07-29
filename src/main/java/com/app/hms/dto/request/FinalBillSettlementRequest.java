package com.app.hms.dto.request;

import com.app.hms.common.Enums.PaymentMode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record FinalBillSettlementRequest(
    @NotNull @PositiveOrZero BigDecimal discount,
    String discountReason,
    @NotNull PaymentMode paymentMode,
    @NotNull @PositiveOrZero BigDecimal paidAmount,
    String paymentReference) {}
