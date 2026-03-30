package com.event.hub.service;

import com.event.hub.db.EventRegistrationRepository;
import com.event.hub.db.EventRepository;
import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventRegistrationEntity;
import com.event.hub.db.entity.EventStatus;
import com.event.hub.db.entity.UserEntity;
import com.event.hub.filter.PageableFilter;
import com.event.hub.model.event.EventDto;
import com.event.hub.model.event.EventMapper;
import com.event.hub.security.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {
    private final AuthenticationService authenticationService;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventMapper eventMapper;

    @Transactional
    public void registrationEvent(Long eventId) {
        EventEntity event = eventService.findById(eventId);
        UserEntity user = authenticationService.getAuthenticatedUserEntity();
        checkingEvent(event, user);
        saveRegistrationEvent(new EventRegistrationEntity(user, event));
    }

    private static void checkingEvent(EventEntity event, UserEntity user) {
        if (!event.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Owner can't register to his own events");
        }
        isNotWaitStartStatusEvent(event, "You cannot register for an event with the status");
        if (event.getMaxPlaces().compareTo(event.getOccupiedPlaces()) <= 0) {
            throw new IllegalStateException("There are no free places for the event ID=%s"
                    .formatted(event.getId())
            );
        }
    }

    @Transactional
    public void cancelRegisteredUserFromTheListParticipantsInThisEventById(Long eventId) {
        EventEntity event = eventService.findById(eventId);
        isNotWaitStartStatusEvent(event, "Can't cancel registration for event which has status");

        Long authenticatedUserId = authenticationService.getCurrentAuthenticatedUserId();
        EventRegistrationEntity registration = eventRegistrationRepository
                .findByUser_IdAndEvent(authenticatedUserId, event)
                .orElseThrow(() -> new EntityNotFoundException("You are not registered in this event ID=%s"
                        .formatted(eventId)
                ));
        cancelRegistrationEvent(registration);
    }

    private static void isNotWaitStartStatusEvent(EventEntity event, String messageError) {
        if (!event.getStatus().equals(EventStatus.WAIT_START.name())) {
            throw new IllegalStateException("%s (ID=%s, STATUS=%s)"
                    .formatted(messageError, event.getId(), event.getStatus()));
        }
    }

    @Transactional(readOnly = true)
    public Page<EventDto> getMyRegistrationsEvent(PageableFilter filter) {
        UserEntity user = authenticationService.getAuthenticatedUserEntity();

        Page<EventDto> pages = eventRegistrationRepository
                .findAllByUser(user, filter.toPageable())
                .map(eventMapper::toDomain);
        return pages.isEmpty() ? Page.empty() : pages;
    }

    @Transactional
    public void saveRegistrationEvent(EventRegistrationEntity registration) {
        if (eventRepository.occupyEmptyPlace(registration.getEvent().getId()) == 0) {
            throw new IllegalStateException("Registered to his event is failed");
        }
        eventRegistrationRepository.save(registration);
    }

    @Transactional
    public void cancelRegistrationEvent(EventRegistrationEntity registration) {
        eventRepository.freeUpOccupiedSpace(registration.getEvent().getId());
        eventRegistrationRepository.delete(registration);
    }
}
