package com.event.hub.service;

import com.event.hub.controller.EventController;
import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventStatus;
import com.event.hub.db.entity.LocationEntity;
import com.event.hub.db.entity.UserEntity;
import com.event.hub.db.entity.UserRole;
import com.event.hub.filter.EventSearchFilter;
import com.event.hub.filter.PageableFilter;
import com.event.hub.model.event.EventDto;
import com.event.hub.model.event.EventMapper;
import com.event.hub.model.event.EventPutRequest;
import com.event.hub.model.user.User;
import com.event.hub.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service("eventManager")
@RequiredArgsConstructor
public class EventManager {
    private final AuthenticationService authenticationService;
    private final EventService eventService;
    private final EventMapper eventMapper;

    private final LocationService locationService;

    @Transactional
    public EventDto createEvent(EventDto eventDto) {
        UserEntity owner = authenticationService.getAuthenticatedUserEntity();
        locationService.validateLocationFromRequestedPlaces(eventDto.getLocationId(), eventDto.getMaxPlaces());
        eventService.checkIfDateIsFree(eventDto);
        EventEntity eventEntity = enrichedEventToSave(eventMapper.toEntity(eventDto), owner);

        return eventMapper.toDomain(eventService.save(eventEntity));
    }

    @Transactional
    public List<EventDto> createTourEvents(List<EventDto> eventDtos) {
        UserEntity owner = authenticationService.getAuthenticatedUserEntity();
        List<EventEntity> events = eventDtos.stream()
                .filter(e -> locationService.validateLocationFromRequestedPlaces(e.getLocationId(), e.getMaxPlaces()))
                .filter(eventService::checkIfDateIsFree)
                .map(eventMapper::toEntity)
                .map(event -> enrichedEventToSave(event, owner))
                .toList();

        return eventService.saveAll(events).stream().map(eventMapper::toDomain).toList();
    }

    public void deleteById(Long id) {
        eventService.updateEventStatusToCanceled(id);
    }

    @Transactional
    public EventDto updateEventById(Long id, EventDto eventDto) {
        eventService.checkIfDateIsFree(eventDto);
        EventEntity existsEvent = eventService.findById(id);
        EventEntity entityToSave = eventMapper.toEntity(eventDto);
        validateMaxAndOccupiedPlaces(entityToSave.getMaxPlaces(), existsEvent.getOccupiedPlaces());
        enrichedEventToUpdate(entityToSave, existsEvent);
        return eventMapper.toDomain(eventService.save(entityToSave));
    }

    public EventDto getEventById(Long id) {
        return eventMapper.toDomain(eventService.findById(id));
    }

    public Page<EventDto> getMyEvents(PageableFilter filter) {
        Long ownerId = authenticationService.getCurrentAuthenticatedUserId();
        Page<EventEntity> myEvents = eventService.findByOwner_Id(ownerId, filter.toPageable());

        return myEvents.isEmpty() ? Page.empty() : myEvents.map(eventMapper::toDomain);
    }

    public Page<EventDto> search(EventSearchFilter filter) {
        return eventService.findAll(filter.toSpecification(), filter.toPageable())
                .map(eventMapper::toDomain);
    }

    /**
     * PreAuthorize is called from {@link EventController}
     * @see EventController#deleteById(Long)
     * @see EventController#updateEventById(Long, EventPutRequest)
     */
    public boolean isEventOwnerOrAdmin(Long eventId) {
        User authUser = authenticationService.getCurrentAuthenticatedUser();
        Long ownerId = getEventById(eventId).getOwnerId();
        var adminAuthority = new SimpleGrantedAuthority(UserRole.ADMIN.name());

        return authUser.getAuthorities().contains(adminAuthority)
                || ownerId.equals(authUser.getId());
    }

    /**
     * @see #createEvent(EventDto)
     * @see #createTourEvents(List)
     */
    private EventEntity enrichedEventToSave(EventEntity event, UserEntity owner) {
        Long locationId = event.getLocation().getId();
        event.setLocation(locationService.findLocationById(locationId));
        event.setOwner(owner);
        event.setStatus(EventStatus.WAIT_START.name());
        event.setOccupiedPlaces(0);
        return event;
    }

    /**
     * @see #updateEventById(Long, EventDto)
     */
    private void enrichedEventToUpdate(EventEntity updateEvent, EventEntity existsEvent) {
        updateEvent.setId(existsEvent.getId());
        updateEvent.setOwner(existsEvent.getOwner());
        updateEvent.setOccupiedPlaces(existsEvent.getOccupiedPlaces());
        updateEvent.setStatus(existsEvent.getStatus());

        Long updateEventLocationId = updateEvent.getLocation().getId();
        if (!existsEvent.getLocation().getId().equals(updateEventLocationId)) {
            LocationEntity updateLocation = locationService.findLocationById(updateEventLocationId);
            locationService.validateLocationFromRequestedPlaces(updateLocation.getId(), existsEvent.getMaxPlaces());
            updateEvent.setLocation(updateLocation);
        } else {
            updateEvent.setLocation(existsEvent.getLocation());
        }
    }

    /**
     * @see #updateEventById(Long, EventDto)
     */
    public void validateMaxAndOccupiedPlaces(Integer maxPlaces, Integer occupiedPlaces) {
        if (maxPlaces.compareTo(occupiedPlaces) < 0) {
            throw new IllegalArgumentException("Max Places can't be less then occupied");
        }
    }
}
