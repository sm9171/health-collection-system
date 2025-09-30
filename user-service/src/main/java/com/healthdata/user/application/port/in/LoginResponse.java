package com.healthdata.user.application.port.in;

public record LoginResponse(
    String accessToken,
    String tokenType,
    Long expiresIn,
    UserResponse user
) {}