package com.auth.api;

import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotNull String refreshToken
) {
}
