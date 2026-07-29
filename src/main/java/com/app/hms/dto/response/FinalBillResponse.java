package com.app.hms.dto.response;

import com.app.hms.common.Enums.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record FinalBillResponse(
    String invoiceNumber,
    BigDecimal grossCharges,
    BigDecimal advanceReceived,
    BigDecimal discount,
    BigDecimal paidAmount,
    BigDecimal balancePayable,
    PaymentMode paymentMode,
    PaymentStatus paymentStatus,
    OffsetDateTime settledAt,
    List<IpdChargeResponse> charges) {}
