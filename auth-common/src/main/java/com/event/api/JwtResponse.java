package com.event.api;

public record JwtResponse(
        String accessToken,
        String refreshToken
) {
}
