package com.app.hms.dto.request;

import com.app.hms.common.Enums.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CreatePharmacySaleRequest(
    Long patientId,
    @Size(max = 160) String customerName,
    @Pattern(regexp = "^$|[0-9]{10,15}$", message = "Customer mobile must contain 10 to 15 digits")
        String customerMobile,
    @NotEmpty List<@Valid Item> items,
    @NotNull @DecimalMin("0.0") BigDecimal discount,
    @NotNull PaymentMode paymentMode) {
  public record Item(@NotNull Long medicineId, @NotNull @Positive Integer quantity) {}
}
