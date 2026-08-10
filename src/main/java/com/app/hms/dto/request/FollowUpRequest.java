package com.app.hms.dto.request;

import jakarta.validation.constraints.*;
import java.time.*;

public record FollowUpRequest(
    @NotNull Long patientId,
    @NotNull @FutureOrPresent LocalDate followUpDate,
    @NotNull LocalTime followUpTime) {}
