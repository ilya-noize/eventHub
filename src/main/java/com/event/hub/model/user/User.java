package com.event.hub.model.user;

import lombok.Builder;

@Builder
public record User(
        Long id,
        String login,
        String password,
        Integer age,
        String role
) {
}
