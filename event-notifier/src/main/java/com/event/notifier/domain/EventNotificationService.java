package com.event.notifier.domain;

import com.event.common.EventType;
import com.event.common.event.EventChange;
import com.event.common.event.EventNotificationPayload;
import com.event.notifier.api.NotificationResponse;
import com.event.notifier.db.Notification;
import com.event.notifier.db.NotificationEventPayload;
import com.event.notifier.db.NotificationEventPayloadRepository;
import com.event.notifier.db.NotificationRepository;
import com.event.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventNotificationService {
    private final AuthorizationService authorizationService;
    private final NotificationRepository notificationRepository;
    private final NotificationEventPayloadRepository notificationEventPayloadRepository;
    private final JsonMapper jacksonJsonMapper;

    @Transactional
    public void markNotificationAsRead(List<Long> ids) {
        Long userId = authorizationService.getCurrentAuthorizedUserId();
        notificationRepository.markNotificationAsRead(userId, ids);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications() {
        Long userId = authorizationService.getCurrentAuthorizedUserId();
        List<Notification> allByUserId = notificationRepository.findAllByUserIdAndHaveReadFalse(userId);
        return allByUserId.isEmpty()
                ? List.of()
                : allByUserId.stream()
                .map(note -> {
                    String eventTypeName = note.getPayload().getEventType();
                    return NotificationResponse.builder()
                            .notificationId(note.getId())
                            .type(eventTypeName)
                            .eventId(note.getEventId())
                            .createdAt(note.getCreatedAt())
                            .isRead(note.isHaveRead())
                            .message(buildMessage(eventTypeName))
                            .payload(note.getPayload().getPayloadJson())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean notExistsByKey(UUID key) {
        return !notificationEventPayloadRepository.existsByMessageId(key);
    }

    @Transactional
    public void save(UUID key, EventNotificationPayload value) {
        List<EventChange> changes = value.getChanges();
        String payloadJson = jacksonJsonMapper.writeValueAsString(changes);

        NotificationEventPayload payload = NotificationEventPayload.builder()
                .messageId(key)
                .eventType(value.getEventType())
                .eventId(value.getEventId())
                .occurredAt(value.getOccurredAt())
                .ownerId(value.getOwnerId())
                .changedById(value.getChangedById())
                .payloadJson(payloadJson)
                .build();
        List<Notification> notifications = value.getSubscribers().stream()
                .map(userId -> Notification.builder()
                        .userId(userId)
                        .payload(payload)
                        .build())
                .toList();
        notificationEventPayloadRepository.save(payload);
        notificationRepository.saveAll(notifications);
    }

    private String buildMessage(String eventTypeName) {
        Map<String, String> messages = Map.of(
                EventType.CREATE.name(), "Вы создали новое событие!",
                EventType.UPDATE.name(), """
                Мы пересмотрели организацию и внесли изменения.
                Теперь всё будет иначе, чтобы каждому было комфортно и весело.
                """,
                EventType.DELETE.name(), """
                К сожалению, мероприятие отменяется.
                Мы долго старались сделать его интересным и полезным, но не получилось.
                Надеемся, что в будущем сможем устроить что-то похожее.
                """
        );
        return messages.get(eventTypeName);
    }
}
