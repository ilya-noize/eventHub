package com.event.hub.service;

import com.event.hub.db.EventRegistrationRepository;
import com.event.hub.db.EventRepository;
import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventRegistrationEntity;
import com.event.hub.db.entity.EventStatus;
import com.event.hub.db.entity.UserEntity;
import com.event.hub.filter.PageableFilter;
import com.event.hub.model.event.Event;
import com.event.hub.model.event.EventMapper;
import com.event.hub.model.user.UserMapper;
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
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<Event> getMyRegistrationsEvent(PageableFilter filter) {
        UserEntity user = userMapper.toEntity(authenticationService.getCurrentAuthenticatedUser());

        return eventRegistrationRepository
                .findAllByUser(user, filter.toPageable())
                .map(eventMapper::toDomain);
    }

    @Transactional
    public void registrationEvent(Long eventId) {
        EventEntity event = eventMapper.toEntity(eventService.getEventById(eventId));
        UserEntity user = userMapper.toEntity(authenticationService.getCurrentAuthenticatedUser());
        if (!event.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Owner can't register to his own events");
        }
        if (!event.getStatus().equals(EventStatus.WAIT_START.name())) {
            throw new IllegalStateException("You cannot register for an event ID=%s with the status=%s"
                    .formatted(eventId, event.getStatus())
            );
        }
        if (event.getMaxPlaces().compareTo(event.getOccupiedPlaces()) <= 0) {
            throw new IllegalStateException("There are no free places for the event ID=%s"
                    .formatted(eventId)
            );
        }
        if (eventRepository.occupyEmptyPlace(eventId) == 0){
            throw new IllegalStateException("Registered to his event is failed");
        }
        eventRegistrationRepository.save(new EventRegistrationEntity(user, event));
    }

    @Transactional
    public void cancelRegisteredUserFromTheListParticipantsInThisEventById(Long eventId) {
        EventEntity event = eventMapper.toEntity(eventService.getEventById(eventId));

        if (!event.getStatus().equals(EventStatus.WAIT_START.name())) {
            throw new IllegalStateException("Can't cancel registration for event which has status=%s"
                    .formatted(event.getStatus())
            );
        }

        Long authenticatedUserId = authenticationService.getCurrentAuthenticatedUserId();
        EventRegistrationEntity registration = eventRegistrationRepository
                .findByUser_IdAndEvent(authenticatedUserId, event)
                .orElseThrow(() -> new EntityNotFoundException("You are not registered in this event ID=%s"
                        .formatted(eventId)
                ));
        eventRegistrationRepository.delete(registration);
        eventRepository.freeUpOccupiedSpace(eventId);
    }
}
