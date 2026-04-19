package com.event.manager.domain.registration;

import com.event.manager.db.EventEntity;
import com.event.manager.db.EventRegistrationEntity;
import com.event.manager.db.EventStatus;
import com.event.manager.domain.event.EventDto;
import com.event.manager.domain.event.EventService;
import com.event.manager.filter.PageableFilter;
import com.event.manager.kafka.EventRegistrationNotificationSender;
import com.event.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventRegistrationManager {
    private final AuthorizationService authorizer;
    private final EventRegistrationNotificationSender sender;
    private final EventRegistrationService registerService;
    private final EventService eventService;

    @Transactional
    public void registrationEvent(Long eventId) {
        Long userId = authorizer.getCurrentAuthorizedUserId();
        EventEntity event = validateEventAndReturn(eventId, userId);
        EventRegistrationEntity registration = EventRegistrationEntity.builder()
                .userId(userId)
                .event(event)
                .build();
        registerService.saveRegistrationEvent(registration);

        sender.sendCancelRegistration(event, userId);
    }

    @Transactional
    public void cancelRegisteredUserFromTheListParticipantsInThisEventById(Long eventId) {
        EventEntity event = getEventWaitingStart(eventId);

        Long userId = authorizer.getCurrentAuthorizedUserId();
        registerService.cancelRegistrationEvent(userId, event);

        sender.sendCreateRegistration(event, userId);
    }

    @Transactional(readOnly = true)
    public Page<EventDto> getMyRegistrationsEvent(PageableFilter filter) {
        Long userId = authorizer.getCurrentAuthorizedUserId();
        return registerService.getMyRegistrationsEvent(userId, filter);
    }

    /**
     * Проверки при попытке зарегистрироваться на мероприятие:<br/>
     * 1 - Нельзя записаться самому себе,<br/>
     * 2 - Можно только в статусе WAIT_START,<br/>
     * 3 - Если есть свободные места.
     *
     * @see #registrationEvent(Long)
     */
    private EventEntity validateEventAndReturn(Long eventId, Long userId) {
        EventEntity event = eventService.findById(eventId); //getEventWaitingStart(eventId, true);
        if (!event.getStatus().equals(EventStatus.WAIT_START.name())) {
            throw new IllegalStateException("Can't register for started or canceled event");
        }
        if (event.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Owner can't register to his own events");
        }
        if (event.getMaxPlaces().compareTo(event.getOccupiedPlaces()) <= 0) {
            throw new IllegalStateException("There are no free places for the event ID=%s"
                    .formatted(event.getId())
            );
        }
        return event;
    }

    /**
     * Проверка статуса перед созданием или отменой регистрации на мероприятие
     *
     * @see #registrationEvent(Long)
     * @see #cancelRegisteredUserFromTheListParticipantsInThisEventById(Long)
     */
    private EventEntity getEventWaitingStart(Long eventId) {
        return eventService.findByIdAndWaitStart(eventId).orElseThrow(
                () -> new IllegalStateException("Can't cancel register for started or canceled event")
        );
    }
}
