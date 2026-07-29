package com.app.hms.dto.request;

import com.app.hms.common.Enums.UserRole;
import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank String username, @NotBlank String password, @NotNull UserRole role) {}
