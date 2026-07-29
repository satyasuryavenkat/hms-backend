package com.app.hms.dto.request;

import jakarta.validation.constraints.NotNull;

public record FinalizeDischargeRequest(@NotNull Long confirmedBy) {}
