package com.app.hms.dto.response;

public record AuthResponse(
    String accessToken, String refreshToken, String tokenType, long expiresIn, UserResponse user) {}
