package com.event.manager.domain.event;


import com.event.common.event.EventChange;
import com.event.common.event.EventNotificationPayload;
import com.event.manager.db.EventEntity;
import com.event.manager.db.EventRegistrationEntity;
import com.event.manager.db.EventStatus;
import com.event.manager.db.LocationEntity;
import com.event.manager.db.TrackChange;
import com.event.manager.kafka.EventNotificationSender;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.event.common.tool.Utils.isDifferentValue;

@Service
@RequiredArgsConstructor
public class EventNotifier {
    private final EventNotificationSender eventNotificationSender;

    @Builder
    public record StatusUpdateNotificationParameters(
            Set<EventEntity> events,
            String status,
            Long currentUserId
    ) {

    }
    @Builder
    public record UpdateNotificationParameters(
            EventEntity updatedEvent,
            EventEntity oldEvent,
            Long currentUserId
    ) {

    }

    public void sendCreateNotification(EventNotificationPayload payload) {
        eventNotificationSender.sendCreateEvent(payload);
    }

    public void sendUpdateNotification(UpdateNotificationParameters parameters) {
        Long currentAuthUserID = parameters.currentUserId();
        List<Long> subscribers = collectNotificationRecipients(parameters.oldEvent, currentAuthUserID);
        List<EventChange> changes = collectChangedFields(parameters);
        eventNotificationSender.sendUpdateEvent(
                EventNotificationPayload.builder()
                        .ownerId(parameters.oldEvent().getOwnerId())
                        .changedById(currentAuthUserID)
                        .subscribers(subscribers)
                        .changes(changes)
                        .build()
        );
    }

    public void sendStatusUpdateNotification(StatusUpdateNotificationParameters params) {
        String status = params.status();
        for(EventEntity event: params.events()) {
            EventChange change = EventChange.builder()
                    .field("status")
                    .oldValue(event.getStatus())
                    .newValue(status)
                    .build();

            EventNotificationPayload payload = EventNotificationPayload.builder()
                    .ownerId(event.getOwnerId())
                    .eventId(event.getId())
                    .subscribers(collectNotificationRecipients(event, params.currentUserId()))
                    .build();
            if (params.currentUserId() != null) {
                payload.setChangedById(params.currentUserId());
            }
            if (EventStatus.CANCELED.name().equals(status)) {
                eventNotificationSender.sendDeleteEvent(payload);
            } else {
                payload.setChanges(List.of(change));
                eventNotificationSender.sendUpdateEvent(payload);
            }
        }
    }

    /**
     * Сравнивает два EventEntity и возвращает список различий в виде NotificationChange.
     * Поля, участвующие в сравнении: name, maxPlaces, date, cost, duration, locationId.
     *
     * @see #sendUpdateNotification(UpdateNotificationParameters)
     */
    private List<EventChange> collectChangedFields(UpdateNotificationParameters parameters) {
        List<EventChange> changes = new ArrayList<>();
        Field[] fields = EventEntity.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.getAnnotation(TrackChange.class) == null) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object oldValue = field.get(parameters.oldEvent());
                Object newValue = field.get(parameters.updatedEvent());

                if ("location".equals(field.getName())) {
                    Long oldLocationId = toLocationId(oldValue);
                    Long newLocationId = toLocationId(newValue);
                    if (isDifferentValue(oldLocationId, newLocationId)) {
                        changes.add(buildChange("locationId", oldLocationId, newLocationId));
                    }
                } else if (isDifferentValue(oldValue, newValue)) {
                    changes.add(buildChange(field.getName(), oldValue, newValue));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Ошибка доступа к полю: " + field.getName(), e);
            }
        }

        return changes;
    }

    private Long toLocationId(Object entity) {
        LocationEntity location = (LocationEntity) entity;
        return location != null ? location.getId() : null;
    }

    private EventChange buildChange(String field,
                                    Object oldValue,
                                    Object newValue) {
        return EventChange.builder()
                .field(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }

    private List<Long> collectNotificationRecipients(EventEntity event,
                                                     Long currentAuthUserID) {
        List<Long> additionalRecipients = new ArrayList<>(Collections.singleton(event.getOwnerId()));
        if(currentAuthUserID != null){
            additionalRecipients.add(currentAuthUserID);
        }
        Stream<Long> registrations = event.getRegistrations().stream()
                .map(EventRegistrationEntity::getUserId);
        return Stream.concat(additionalRecipients.stream(), registrations).distinct().toList();
    }
}
