package com.event.hub.model.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record UserRegistration(
        @NotBlank
        String login,

        @NotBlank
        String password,

        @NotNull
        @Positive
        Integer age
) {
}
