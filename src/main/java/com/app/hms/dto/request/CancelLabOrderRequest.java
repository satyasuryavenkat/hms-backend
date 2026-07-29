package com.app.hms.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelLabOrderRequest(@NotBlank String reason) {}
