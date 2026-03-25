package com.event.hub.service;

import com.event.hub.db.EventRegistrationRepository;
import com.event.hub.db.EventRepository;
import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventRegistrationEntity;
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
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    public Page<Event> getMyRegistrationsEvent(PageableFilter filter) {
        UserEntity user = userMapper.toEntity(authenticationService.getCurrentAuthenticatedUser());

        return eventRegistrationRepository
                .findAllByUser(user, filter.toPageable())
                .map(eventMapper::toDomain);
    }

    @Transactional
    public void registrationEvent(Long eventId) {
        EventEntity event = eventRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(String.format("No such event ID=%s", eventId))
        );
        UserEntity user = userMapper.toEntity(authenticationService.getCurrentAuthenticatedUser());
        if (!event.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Owner can't register to his own events");
        }
        if (!eventRepository.occupyEmptyPlace(eventId)) {
            throw new IllegalStateException("Not enough places");
        }
        eventRegistrationRepository.save(new EventRegistrationEntity(user, event));
    }

    public void cancelRegisteredUserFromTheListParticipantsInThisEventById(Long eventId) {
        UserEntity user = userMapper.toEntity(authenticationService.getCurrentAuthenticatedUser());
        EventRegistrationEntity registration = eventRegistrationRepository
                .findByUser_IdAndEvent_Id(user.getId(), eventId)
                .orElseThrow(() -> new EntityNotFoundException("You are not registered in this event ID=%s"
                        .formatted(eventId)
                ));
        eventRegistrationRepository.delete(registration);
        eventRepository.freeUpOccupiedSpace(eventId);
    }
}
