package com.event.hub.service;

import com.event.hub.db.EventRepository;
import com.event.hub.db.LocationRepository;
import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventStatus;
import com.event.hub.db.entity.LocationEntity;
import com.event.hub.filter.EventSearchFilter;
import com.event.hub.filter.PageableFilter;
import com.event.hub.model.event.Event;
import com.event.hub.model.event.EventMapper;
import com.event.hub.security.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final AuthenticationService authenticationService;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;

    @Transactional
    public Event createEvent(Event event) {
        event.setOwnerId(authenticationService.getCurrentAuthenticatedUserId());

        event.setStatus(EventStatus.WAIT_START.name());
        event.setOccupiedPlaces(0);
        EventEntity eventEntity = eventMapper.toEntity(event);

        LocationEntity location = getConfirmedLocationForEvent(event);
        eventEntity.setLocation(location);

        return eventMapper.toDomain(eventRepository.save(eventEntity));
    }

    @Transactional
    public List<Event> createEventTour(List<Event> events) {
        return events.stream()
                .map(this::createEvent)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        Event event = getEventById(id);
        authenticationService.verifyAuthenticatedUserAsOwnerResourceOrAdmin(event.getOwnerId());
        eventRepository.updateEventStatusToCanceled(id);
    }

    @Transactional
    public Event updateEventById(Long id, Event event) {
        Event existsEvent = getEventById(id);

        Long ownerId = existsEvent.getOwnerId();
        authenticationService.verifyAuthenticatedUserAsOwnerResourceOrAdmin(ownerId);

        if(event.getMaxPlaces() < existsEvent.getOccupiedPlaces()) {
            throw new IllegalArgumentException("Max Places can't be less then occupied");
        }
        event.setId(existsEvent.getId());
        event.setOwnerId(ownerId);
        event.setOccupiedPlaces(existsEvent.getOccupiedPlaces());
        event.setStatus(existsEvent.getStatus());

        EventEntity entityToSave = eventMapper.toEntity(event);

        Long newLocationId = event.getLocationId();
        if (!newLocationId.equals(existsEvent.getLocationId())) {
            LocationEntity location = getConfirmedLocationForEvent(event);
            entityToSave.setLocation(location);
        }

        return eventMapper.toDomain(eventRepository.saveAndFlush(entityToSave));
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

    @Transactional
    @Scheduled(cron = "0 * * ? * *", zone = "Europe/Moscow")
    @Async
    public void updateAllEventsStatusToStarted() {
        int updated = eventRepository.updateAllEventsStatusToStarted();
        loggingResultsUpdateStatus(updated, EventStatus.STARTED);
    }

    @Transactional
    @Scheduled(cron = "1 * * ? * *", zone = "Europe/Moscow")
    @Async
    public void updateAllEventsStatusToFinished() {
        int updated = eventRepository.updateAllEventsStatusToFinished();
        loggingResultsUpdateStatus(updated, EventStatus.FINISHED);
    }

    private static void loggingResultsUpdateStatus(int updated, EventStatus status) {
        if (updated != 0) {
            log.info("{} Events status changed to {}", updated, status);
        }
    }

    private LocationEntity getConfirmedLocationForEvent(Event event) {
        LocationEntity location = locationRepository.findById(event.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("No such location ID=%s"
                        .formatted(event.getLocationId()))
                );
        if (location.getCapacity() < event.getMaxPlaces()) {
            throw new IllegalArgumentException("%s places is more than %s capacity in %s place ID=%s"
                    .formatted(
                            event.getMaxPlaces(),
                            location.getCapacity(),
                            location.getName(),
                            event.getLocationId()
                    ));
        }
        return location;
    }
}
