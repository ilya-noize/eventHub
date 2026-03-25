package com.event.hub.service;

import com.event.hub.db.EventRepository;
import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventStatus;
import com.event.hub.filter.EventSearchFilter;
import com.event.hub.filter.PageableFilter;
import com.event.hub.model.event.Event;
import com.event.hub.model.event.EventMapper;
import com.event.hub.model.location.Location;
import com.event.hub.security.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final AuthenticationService authenticationService;
    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final EventMapper eventMapper;

    @Transactional
    public Event createEvent(Event event) {
        event.setOwnerId(authenticationService.getCurrentAuthenticatedUserId());

        Location location = locationService.getConfirmedLocationForEventCapacity(
                event.getLocationId(),
                event.getMaxPlaces()
        );
        event.setLocationId(location.id());

        event.setStatus(EventStatus.WAIT_START.name());
        event.setOccupiedPlaces(0);

        EventEntity eventEntity = eventMapper.toEntity(event);
        return eventMapper.toDomain(eventRepository.save(eventEntity));
    }

    @Transactional
    public void deleteById(Long id) {
        Event event = getEventById(id);
        authenticationService.verifyAuthenticatedUserAsOwnerResource(event.getOwnerId());
        eventRepository.deleteById(id);
    }

    @Transactional
    public Event updateEventById(Long id, Event event) {
        Event existsEvent = getEventById(id);

        Long ownerId = existsEvent.getOwnerId();
        authenticationService.verifyAuthenticatedUserAsOwnerResource(ownerId);

        event.setId(existsEvent.getId());
        event.setOwnerId(ownerId);
        event.setOccupiedPlaces(existsEvent.getOccupiedPlaces());
        event.setStatus(existsEvent.getStatus());


        Long newLocationId = event.getLocationId();
        if (!newLocationId.equals(existsEvent.getLocationId())) {
            Location location = locationService.getConfirmedLocationForEventCapacity(
                    event.getLocationId(),
                    event.getMaxPlaces()
            );
            event.setLocationId(location.id());
        }
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
        Long ownerId = authenticationService.getCurrentAuthenticatedUserId();
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
