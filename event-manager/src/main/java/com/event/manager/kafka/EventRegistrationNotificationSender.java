package com.event.manager.kafka;

import com.event.common.EventType;
import com.event.common.registration.EventRegistrationNotificationPayload;
import com.event.manager.db.EventEntity;
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
public class EventRegistrationNotificationSender implements NotificationSender<EventRegistrationNotificationPayload> {
    private final KafkaTemplate<UUID, EventRegistrationNotificationPayload> kafkaTemplate;

    @Override
    public void send(EventRegistrationNotificationPayload payload) {
        payload.setMessageId(UUID.randomUUID());
        payload.setOccurredAt(LocalDateTime.now());
        log.info("Sending notification: {}", payload.getMessageId());

        var future = kafkaTemplate.send("registration-topic", payload.getMessageId(), payload);

        future.whenComplete(
                (result, e) -> log.debug("Sent notification {} successfully", result.getProducerRecord().key())
        );
    }

    public void sendCreateRegistration(EventEntity event, Long userId) {
        var payload = buildEventRegistrationNotificationPayload(event, userId);
        payload.setEventType(EventType.CREATE.name());
        send(payload);
    }

    public void sendCancelRegistration(EventEntity event, Long userId) {
        var payload = buildEventRegistrationNotificationPayload(event, userId);
        payload.setEventType(EventType.DELETE.name());
        send(payload);
    }

    private EventRegistrationNotificationPayload buildEventRegistrationNotificationPayload(
            EventEntity event,
            Long userId
    ) {
        return EventRegistrationNotificationPayload.builder()
                .eventId(event.getId())
                .subscribers(List.of(event.getOwnerId(), userId))
                .build();
    }
}
