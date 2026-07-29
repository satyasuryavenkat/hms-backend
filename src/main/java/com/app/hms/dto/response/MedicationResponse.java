package com.app.hms.dto.response;

public record MedicationResponse(
    String medicine, String dose, String frequency, String duration, String instructions) {}
