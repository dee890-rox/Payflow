package com.payflow.payflow.dto;

public record AuthResponse(
    String tokenType,
    String accessToken,
    long expiresInMs,
    UserResponse user,
    WalletResponse wallet
) {}
