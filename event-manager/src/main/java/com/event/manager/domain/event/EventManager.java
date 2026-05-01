package com.event.manager.domain.event;

import com.event.common.UserRole;
import com.event.common.event.EventChange;
import com.event.common.event.EventNotificationPayload;
import com.event.domain.UserDto;
import com.event.manager.api.event.EventController;
import com.event.manager.api.event.EventPutRequest;
import com.event.manager.db.EventEntity;
import com.event.manager.db.EventRegistrationEntity;
import com.event.manager.db.EventStatus;
import com.event.manager.db.LocationEntity;
import com.event.manager.db.TrackChange;
import com.event.manager.domain.EventMapper;
import com.event.manager.domain.location.LocationManager;
import com.event.manager.filter.EventSearchFilter;
import com.event.manager.filter.PageableFilter;
import com.event.manager.kafka.EventNotificationSender;
import com.event.security.AuthorizationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.event.common.tool.Utils.isDifferentValue;

@Slf4j
@Service("eventManager")
@RequiredArgsConstructor
public class EventManager {
    private static final String CACHE_PREFIX_EVENT = "event::";
    private final RedisTemplate<String, EventEntity> redisTemplate;
    private final AuthorizationService authorizationService;
    private final EventNotificationSender eventNotificationSender;
    private final EventService eventService;
    private final EventMapper eventMapper;

    private final LocationManager locationService;

    @Transactional
    public EventDto createEvent(EventDto eventDto) {
        Long ownerId = authorizationService.getCurrentAuthorizedUserId();
        locationService.validateLocationFromRequestedPlaces(eventDto.getLocationId(), eventDto.getMaxPlaces());
        eventService.checkIfDateIsFree(eventDto);
        EventEntity eventEntity = enrichedEventToSave(eventMapper.toEntity(eventDto), ownerId);

        EventEntity saved = eventService.save(eventEntity);

        eventNotificationSender.sendCreateEvent(EventNotificationPayload.builder()
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
                .filter(eventService::checkIfDateIsFree)
                .map(eventMapper::toEntity)
                .map(event -> enrichedEventToSave(event, ownerId))
                .toList();

        List<EventEntity> savedAllEvents = eventService.saveAll(events);

        savedAllEvents.forEach(event -> eventNotificationSender.sendCreateEvent(
                EventNotificationPayload.builder()
                        .ownerId(ownerId)
                        .eventId(event.getId())
                        .subscribers(List.of(ownerId))
                        .build()
        ));

        return savedAllEvents.stream().map(eventMapper::toDomain).toList();
    }

    public void deleteById(Long id) {
        EventEntity event = redisTemplate.opsForValue().getAndDelete(cacheKey(id));
        if (event == null) {
            event = eventService.findByIdAndWaitStartWithRegistrations(id).orElseThrow(
                    () -> new EntityNotFoundException("No such event to delete. ID=%s".formatted(id))
            );
        }
        if (!event.getStatus().equals(EventStatus.WAIT_START.name())) {
            throw new IllegalStateException("Can't cancel the started or finished event");
        }
        eventService.updateEventStatusToCanceled(id);

        Long ownerId = event.getOwnerId();
        Long currentAuthUserID = authorizationService.getCurrentAuthorizedUserId();
        List<Long> subscribers = getSubscribersWithCurrentAuthUser(currentAuthUserID, event);

        eventNotificationSender.sendDeleteEvent(EventNotificationPayload.builder()
                .ownerId(ownerId)
                .eventId(event.getId())
                .subscribers(subscribers)
                .changedById(currentAuthUserID)
                .build()
        );
    }

    @Transactional
    public EventDto updateEventById(Long id, EventDto eventDto) {
        eventService.checkIfDateIsFree(eventDto);
        EventEntity existsEvent = redisTemplate.opsForValue().getAndDelete(cacheKey(id));
        if (existsEvent == null) {
            existsEvent = eventService.findByIdAndWaitStartWithRegistrations(id).orElseThrow(
                    () -> new EntityNotFoundException("No such event to update. ID=%s".formatted(id))
            );
        }
        EventEntity entityToSave = eventMapper.toEntity(eventDto);

        validateMaxAndOccupiedPlaces(entityToSave.getMaxPlaces(), existsEvent.getOccupiedPlaces());
        Long ownerId = existsEvent.getOwnerId();
        Long currentAuthorizedUserId = authorizationService.getCurrentAuthorizedUserId();
        List<Long> subscribers = getSubscribersWithCurrentAuthUser(currentAuthorizedUserId, existsEvent);
        List<EventChange> changes = buildChanges(existsEvent, entityToSave);

        eventNotificationSender.sendUpdateEvent(
                EventNotificationPayload.builder()
                        .ownerId(ownerId)
                        .changedById(currentAuthorizedUserId)
                        .subscribers(subscribers)
                        .changes(changes)
                        .build()
        );

        enrichedEventToUpdate(entityToSave, existsEvent);
        eventService.save(entityToSave);

        return eventMapper.toDomain(entityToSave);
    }

    @Cacheable(value = "event", key = "#id")
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
        Long ownerId = getEventById(eventId).getOwnerId();
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
     * Проверка на то что количество мест не меньше занятых
     *
     * @see #updateEventById(Long, EventDto)
     */
    public void validateMaxAndOccupiedPlaces(Integer maxPlaces, Integer occupiedPlaces) {
        if (maxPlaces.compareTo(occupiedPlaces) < 0) {
            throw new IllegalArgumentException("Max Places can't be less then occupied");
        }
    }

    /**
     * @return Все подписчики события с текущим пользователем как редактором или участником
     * @see #updateEventById(Long, EventDto)
     * @see #deleteById(Long)
     */
    private List<Long> getSubscribersWithCurrentAuthUser(Long currentAuthUserID, EventEntity event) {
        Stream<Long> editors = Stream.of(event.getOwnerId(), currentAuthUserID);
        Stream<Long> registrations = event.getRegistrations().stream()
                .map(EventRegistrationEntity::getUserId);
        return Stream.concat(editors, registrations).distinct().toList();
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

    /**
     * Сравнивает два EventEntity и возвращает список различий в виде NotificationChange.
     * Поля, участвующие в сравнении: name, maxPlaces, date, cost, duration, locationId.
     *
     * @see #updateEventById(Long, EventDto)
     */
    private List<EventChange> buildChanges(EventEntity updated, EventEntity existing) {
        List<EventChange> changes = new ArrayList<>();
        Field[] fields = EventEntity.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.getAnnotation(TrackChange.class) == null) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object oldValue = field.get(existing);
                Object newValue = field.get(updated);

                if ("location".equals(field.getName())) {
                    Long oldLocationId = extractLocationId(oldValue);
                    Long newLocationId = extractLocationId(newValue);
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

    private Long extractLocationId(Object entity) {
        LocationEntity location = (LocationEntity) entity;
        return location != null ? location.getId() : null;
    }

    private EventChange buildChange(String field, Object oldValue, Object newValue) {
        return EventChange.builder()
                .field(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }

    private String cacheKey(Long eventId) {
        return CACHE_PREFIX_EVENT.concat(":").concat((String.valueOf(eventId)).intern());
    }
}
