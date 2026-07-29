package com.app.hms.dto.request;

import com.app.hms.common.Enums.Gender;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PatientRequest(
    @NotBlank String name,
    @NotBlank @Pattern(regexp = "[0-9]{10,15}") String mobile,
    @NotNull @PastOrPresent LocalDate dateOfBirth,
    @NotNull Gender gender,
    String address) {}
