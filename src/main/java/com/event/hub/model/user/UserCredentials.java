package com.event.hub.model.user;

import jakarta.validation.constraints.NotBlank;

public record UserCredentials(
        @NotBlank
        String login,

        @NotBlank
        String password
) {
}
