package com.event.manager.domain.event;

import com.event.common.UserRole;
import com.event.common.event.EventNotificationPayload;
import com.event.domain.UserDto;
import com.event.manager.api.event.EventController;
import com.event.manager.api.event.EventPutRequest;
import com.event.manager.db.EventEntity;
import com.event.manager.db.EventStatus;
import com.event.manager.db.LocationEntity;
import com.event.manager.domain.EventMapper;
import com.event.manager.domain.location.LocationManager;
import com.event.manager.filter.EventSearchFilter;
import com.event.manager.filter.PageableFilter;
import com.event.security.AuthorizationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.event.manager.domain.event.EventNotifier.StatusUpdateNotificationParameters;
import static com.event.manager.domain.event.EventNotifier.UpdateNotificationParameters;

@Slf4j
@Service("eventManager")
@RequiredArgsConstructor
public class EventManager {
    private static final String CACHE_PREFIX_EVENT = "event";

    private final RedisTemplate<String, EventEntity> redisTemplate;
    private final AuthorizationService authorizationService;
    private final EventValidationService eventValidationService;
    private final EventService eventService;
    private final LocationManager locationService;
    private final EventNotifier eventNotifier;
    private final EventMapper eventMapper;



    @Transactional
    public EventDto createEvent(EventDto eventDto) {
        Long ownerId = authorizationService.getCurrentAuthorizedUserId();
        locationService.validateLocationFromRequestedPlaces(eventDto.getLocationId(), eventDto.getMaxPlaces());
        eventValidationService.checkIfDateIsFree(eventDto);
        EventEntity eventEntity = enrichedEventToSave(eventMapper.toEntity(eventDto), ownerId);

        EventEntity saved = eventService.save(eventEntity);

        eventNotifier.sendCreateNotification(EventNotificationPayload.builder()
                .ownerId(ownerId)
                .eventId(saved.getId())
                .subscribers(List.of(ownerId))
                .build());

        return eventMapper.toDomain(saved);
    }

    @Transactional
    public List<EventDto> createTourEvents(List<EventDto> eventDtos) {
        Long ownerId = authorizationService.getCurrentAuthorizedUserId();
        List<EventEntity> events = eventDtos.stream()
                .filter(e -> locationService.validateLocationFromRequestedPlaces(e.getLocationId(), e.getMaxPlaces()))
                .filter(eventValidationService::checkIfDateIsFree)
                .map(eventMapper::toEntity)
                .map(event -> enrichedEventToSave(event, ownerId))
                .toList();

        List<EventEntity> savedAllEvents = eventService.saveAll(events);

        savedAllEvents.forEach(event -> eventNotifier.sendCreateNotification(
                EventNotificationPayload.builder()
                        .ownerId(ownerId)
                        .eventId(event.getId())
                        .subscribers(List.of(ownerId))
                        .build()
        ));

        return savedAllEvents.stream().map(eventMapper::toDomain).toList();
    }

    @CacheEvict(value = CACHE_PREFIX_EVENT, key = "#id")
    public void deleteById(Long id) {
        EventEntity event = redisTemplate.opsForValue().getAndDelete(cacheKey(id));
        if (event == null || event.notStartedYet()) {
            event = eventService.findByIdAndWaitStartWithRegistrations(id).orElseThrow(
                    () -> new EntityNotFoundException("No such event to delete. ID=%s".formatted(id))
            );
        }
        if (!event.notStartedYet()) {
            throw new IllegalStateException("Can't cancel. Event not waiting start");
        }
        eventService.updateEventStatusToCanceledById(id);

        Long currentAuthorizedUserId = authorizationService.getCurrentAuthorizedUserId();
        var changeStatusNotificationParameters = StatusUpdateNotificationParameters.builder()
                .events(Set.of(event))
                .status(EventStatus.CANCELED.name())
                .currentUserId(currentAuthorizedUserId).build();
        eventNotifier.sendStatusUpdateNotification(changeStatusNotificationParameters);

    }

    @Transactional
    @CachePut(value = CACHE_PREFIX_EVENT, key = "#eventDto.id")
    public EventDto updateEventById(Long id, EventDto eventDto) {
        if (!Objects.equals(id, eventDto.getId())) throw new IllegalArgumentException("ID mismatch");
        eventValidationService.checkIfDateIsFree(eventDto);
        EventEntity oldEvent = redisTemplate.opsForValue().getAndDelete(cacheKey(id));
        if (oldEvent == null || oldEvent.notStartedYet()) {
            oldEvent = eventService.findByIdAndWaitStartWithRegistrations(id).orElseThrow(
                    () -> new EntityNotFoundException("No such event to update. ID=%s".formatted(id))
            );
        }
        EventEntity updatedEvent = eventMapper.toEntity(eventDto);

        eventValidationService.validateMaxAndOccupiedPlaces(updatedEvent.getMaxPlaces(), oldEvent.getOccupiedPlaces());
        enrichedEventToUpdate(updatedEvent, oldEvent);
        eventService.save(updatedEvent);

        var parameters = UpdateNotificationParameters.builder()
                .updatedEvent(updatedEvent)
                .oldEvent(oldEvent)
                .currentUserId(authorizationService.getCurrentAuthorizedUserId())
                .build();
        eventNotifier.sendUpdateNotification(parameters);

        return eventMapper.toDomain(updatedEvent);
    }

    @Cacheable(value = "event", key = "#id", unless = "#result == null")
    public EventDto getEventById(Long id) {
        return eventMapper.toDomain(eventService.findById(id));
    }

    public Page<EventDto> getMyEvents(PageableFilter filter) {
        Long ownerId = authorizationService.getCurrentAuthorizedUserId();
        Page<EventEntity> myEvents = eventService.findByOwner_Id(ownerId, filter.toPageable());

        return myEvents.isEmpty() ? Page.empty() : myEvents.map(eventMapper::toDomain);
    }

    public Page<EventDto> search(EventSearchFilter filter) {
        return eventService.findAll(filter.toSpecification(), filter.toPageable())
                .map(eventMapper::toDomain);
    }

    /**
     * PreAuthorize is called from {@link EventController}
     *
     * @see EventController#deleteById(Long)
     * @see EventController#updateEventById(Long, EventPutRequest)
     */
    public boolean isEventOwnerOrAdmin(Long eventId) {
        UserDto authUserDto = authorizationService.getCurrentAuthorizedUser();

        EventEntity event = redisTemplate.opsForValue().get(cacheKey(eventId));
        Long ownerId = event != null ? event.getOwnerId() : getEventById(eventId).getOwnerId();
        var adminAuthority = new SimpleGrantedAuthority(UserRole.ADMIN.name());

        return authUserDto.getAuthorities().contains(adminAuthority)
               || ownerId.equals(authUserDto.getId());
    }

    /**
     * @see #createEvent(EventDto)
     * @see #createTourEvents(List)
     */
    private EventEntity enrichedEventToSave(EventEntity event, Long ownerId) {
        Long locationId = event.getLocation().getId();
        event.setLocation(locationService.findLocationById(locationId));
        event.setOwnerId(ownerId);
        event.setStatus(EventStatus.WAIT_START.name());
        event.setOccupiedPlaces(0);
        return event;
    }

    /**
     * Обновляет поля существующего объекта из обновляемого
     *
     * @see #updateEventById(Long, EventDto)
     */
    private void enrichedEventToUpdate(EventEntity updateEvent, EventEntity existsEvent) {
        updateEvent.setId(existsEvent.getId());
        updateEvent.setOwnerId(existsEvent.getOwnerId());
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

    private String cacheKey(Long eventId) {
        return CACHE_PREFIX_EVENT + ":" + eventId;
    }
}
