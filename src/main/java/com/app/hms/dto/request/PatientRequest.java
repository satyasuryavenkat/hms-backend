package com.app.hms.dto.request;

import com.app.hms.common.Enums.Gender;
import jakarta.validation.constraints.*;
public record PatientRequest(
    @NotBlank String name,
    @NotBlank @Pattern(regexp = "[0-9]{10,15}") String mobile,
    @NotNull @Min(0) @Max(130) Integer age,
    @NotNull Gender gender,
    String address) {}
