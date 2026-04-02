package com.event.hub.model.user;

public record UserResponse(
        Long id,
        String login,
        Integer age,
        String role
) {
}
