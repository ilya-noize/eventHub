package com.event.hub.model.user;

public record User(
        Long id,
        String login,
        String password,
        String age,
        String role
) {
}
