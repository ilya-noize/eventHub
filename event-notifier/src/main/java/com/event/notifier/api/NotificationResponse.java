package com.event.notifier.api;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long notificationId,
        String type,
        Long eventId,
        LocalDateTime createdAt,
        Boolean isRead,
        String message,
        String payload
) {
}