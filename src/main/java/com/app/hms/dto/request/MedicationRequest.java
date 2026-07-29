package com.app.hms.dto.request;

public record MedicationRequest(
    String medicine, String dose, String frequency, String duration, String instructions) {}
