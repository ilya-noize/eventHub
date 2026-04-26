package com.event.manager.kafka;

import com.event.common.EventType;
import com.event.common.event.EventNotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventNotificationSender implements NotificationSender<EventNotificationPayload> {
    private final KafkaTemplate<UUID, EventNotificationPayload> kafkaTemplate;

    @Override
    public void send(EventNotificationPayload payload) {
        payload.setMessageId(UUID.randomUUID());
        payload.setOccurredAt(LocalDateTime.now());
        EventNotificationSender.log.info("Sending notification: {}", payload.getMessageId());

        var future = kafkaTemplate.send("event-topic", payload.getMessageId(), payload);

        future.whenComplete((result, e) -> {
            EventNotificationSender.log.debug("Sent notification {} successfully", result.getProducerRecord().key());
        });
    }

    public void sendCreateEvent(EventNotificationPayload payload) {
        payload.setEventType(EventType.CREATE.name());
        payload.setChangedById(null);
        payload.setChanges(List.of());
        send(payload);
    }

    public void sendUpdateEvent(EventNotificationPayload payload) {
        payload.setEventType(EventType.UPDATE.name());
        send(payload);
    }

    public void sendDeleteEvent(EventNotificationPayload payload) {
        payload.setEventType(EventType.DELETE.name());
        payload.setChanges(List.of());
        send(payload);
    }
}
