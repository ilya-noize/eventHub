package com.event.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventNotificationPayload {
    private UUID messageId;
    private String eventType;

    private Long eventId;
    private LocalDateTime occurredAt; // Произошедшее

    private Long ownerId;
    private Long changedById;

    private List<Long> subscribers;
    private List<EventChange> changes;
}

