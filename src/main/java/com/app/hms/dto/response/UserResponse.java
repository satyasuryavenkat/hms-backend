package com.app.hms.dto.response;

import com.app.hms.common.Enums.UserRole;
import java.util.Set;

public record UserResponse(
    Long id,
    String username,
    String displayName,
    UserRole role,
    boolean active,
    String initials,
    Set<String> permissions) {}
