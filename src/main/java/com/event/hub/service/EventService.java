package com.event.hub.service;

import com.event.hub.db.EventRepository;
import com.event.hub.db.LocationRepository;
import com.event.hub.db.UserRepository;
import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventStatus;
import com.event.hub.filter.EventSearchFilter;
import com.event.hub.filter.PageableFilter;
import com.event.hub.model.event.Event;
import com.event.hub.model.event.EventMapper;
import com.event.hub.model.user.User;
import com.event.hub.security.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventService {
    private final AuthenticationService authenticationService;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Transactional
    public Event createEvent(Event domain) {

        User user = authenticationService.getCurrentAuthenticatedUser();
        EventEntity event = eventMapper.toEntity(domain);
        event.setOwner(userRepository.getReferenceById(user.getId()));
        event.setLocation(locationRepository.findById(domain.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("No such location. ID=%s"
                        .formatted(domain.getLocationId())
                ))
        );
        event.setStatus(EventStatus.WAIT_START.name());
        event.setOccupiedPlaces(0);
        event.setRegistrations(Set.of());

        return eventMapper.toDomain(eventRepository.save(event));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("No such event. ID=%s".formatted(id));
        }
        eventRepository.deleteById(id);
    }

    @Transactional
    public Event updateEventById(Long id, Event event) {
        Event existsEvent = getEventById(id);
        User user = authenticationService.getCurrentAuthenticatedUser();
        if (!existsEvent.getOwnerId().equals(user.getId())) {
            throw new SecurityException("You are trying to edit someone else's events");
        }
        event.setId(existsEvent.getId());
        event.setOwnerId(existsEvent.getOwnerId());
        event.setOccupiedPlaces(existsEvent.getOccupiedPlaces());
        event.setStatus(existsEvent.getStatus());

        EventEntity entityToSave = eventMapper.toEntity(event);

        return eventMapper.toDomain(eventRepository.save(entityToSave));
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .map(eventMapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("No such event. ID=%s"
                        .formatted(id))
                );
    }

    @Transactional(readOnly = true)
    public Page<Event> getMyEvents(PageableFilter filter) {
        Long ownerId = authenticationService.getCurrentAuthenticatedUser().getId();
        Page<EventEntity> myEvents = eventRepository.findByOwner_Id(ownerId, filter.toPageable());

        return myEvents.map(eventMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Page<Event> search(EventSearchFilter filter) {
        Page<EventEntity> findAll = eventRepository.findAll(
                filter.toSpecification(),
                filter.toPageable()
        );
        return findAll.map(eventMapper::toDomain);
    }
}
