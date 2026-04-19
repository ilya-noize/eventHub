package com.event.api;

import com.event.domain.UserDto;
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
    public UserDto mappingDto() {
        return UserDto.builder()
                .login(login())
                .password(password())
                .age(age())
                .build();
    }
}
