package com.app.hms.dto.request;

import com.app.hms.common.Enums.UserRole;
import jakarta.validation.constraints.*;
import java.util.Set;

public record UpdateUserRequest(
    @NotBlank String username,
    @NotBlank String displayName,
    @Size(min = 6) String password,
    @NotNull UserRole role,
    boolean active,
    Set<String> permissions) {}
