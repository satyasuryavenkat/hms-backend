package com.app.hms.dto.request;

import jakarta.validation.constraints.*;

public record AttendantRequest(
    @NotBlank String name, @NotBlank @Pattern(regexp = "[0-9]{10,15}") String mobile) {}
