package com.app.hms.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record IpdChargesResponse(
    Long admissionId, List<IpdChargeResponse> charges, BigDecimal grossAmount) {}
