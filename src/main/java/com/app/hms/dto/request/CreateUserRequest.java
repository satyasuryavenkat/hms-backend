package com.app.hms.dto.request;

import com.app.hms.common.Enums.UserRole;
import jakarta.validation.constraints.*;
import java.util.Set;

public record CreateUserRequest(
    @NotBlank String username,
    @NotBlank String displayName,
    @NotBlank @Size(min = 6) String password,
    @NotNull UserRole role,
    boolean active,
    Set<String> permissions) {}
