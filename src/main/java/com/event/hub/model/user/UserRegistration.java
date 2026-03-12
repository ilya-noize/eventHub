package com.event.hub.model.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRegistration(
        @NotBlank
        String login,

        @NotBlank
        String password,

        @NotNull
        Integer age
) {
}
