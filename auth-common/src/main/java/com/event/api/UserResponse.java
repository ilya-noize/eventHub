package com.event.api;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String login,
        Integer age,
        String role
) {
}
