package com.event.hub.model.user;

import com.event.hub.db.entity.UserRole;

public record UserResponse(
        Long id,
        String login,
        String age,
        UserRole role
) {
}
