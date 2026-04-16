package com.auth.api;

public record JwtResponse(
        String accessToken,
        String refreshToken
) {
}
