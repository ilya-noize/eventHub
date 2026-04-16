package com.event.notifier.api;

import java.util.List;

public record MarkNotificationAsReadRequest(List<Long> notificationIds) {
}
