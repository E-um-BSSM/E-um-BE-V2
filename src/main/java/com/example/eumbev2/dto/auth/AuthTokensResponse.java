package com.example.eumbev2.dto.auth;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
    public static AuthTokensResponse of(String accessToken, String refreshToken, long expiresInSeconds) {
        return new AuthTokensResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}
