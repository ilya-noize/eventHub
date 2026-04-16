package com.event.manager.domain.event;

import com.auth.domain.UserDto;
import com.auth.security.AuthorizationService;
import com.event.common.UserRole;
import com.event.common.event.EventChange;
import com.event.common.event.EventNotificationPayload;
import com.event.manager.api.event.EventController;
import com.event.manager.api.event.EventPutRequest;
import com.event.manager.db.entity.EventEntity;
import com.event.manager.db.entity.EventRegistrationEntity;
import com.event.manager.db.entity.EventStatus;
import com.event.manager.db.entity.LocationEntity;
import com.event.manager.domain.EventMapper;
import com.event.manager.domain.location.LocationService;
import com.event.manager.filter.EventSearchFilter;
import com.event.manager.filter.PageableFilter;
import com.event.manager.kafka.EventNotificationSender;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.event.common.tool.Utils.isDifferentValue;

@Slf4j
@Service("eventManager")
@RequiredArgsConstructor
public class EventManager {
    private final AuthorizationService authorizationService;
    private final EventNotificationSender eventNotificationSender;
    private final EventService eventService;
    private final EventMapper eventMapper;

    private final LocationService locationService;

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
        EventEntity event = eventService.findByIdAndWaitStartWithRegistrations(id).orElseThrow(
                () -> new EntityNotFoundException("No such event to delete. ID=%s".formatted(id))
        );
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

        EventEntity existsEvent = eventService.findByIdAndWaitStartWithRegistrations(id).orElseThrow(
                () -> new EntityNotFoundException("No such event to update. ID=%s".formatted(id))
        );
        EventEntity entityToSave = eventMapper.toEntity(eventDto);

        validateMaxAndOccupiedPlaces(entityToSave.getMaxPlaces(), existsEvent.getOccupiedPlaces());
        Long ownerId = existsEvent.getOwnerId();
        Long currentAuthUserID = authorizationService.getCurrentAuthorizedUserId();
        List<Long> subscribers = getSubscribersWithCurrentAuthUser(currentAuthUserID, existsEvent);
        List<EventChange> changes = buildChanges(existsEvent, entityToSave);

        eventNotificationSender.sendUpdateEvent(
                EventNotificationPayload.builder()
                        .ownerId(ownerId)
                        .changedById(currentAuthUserID)
                        .subscribers(subscribers)
                        .changes(changes)
                        .build()
        );

        enrichedEventToUpdate(entityToSave, existsEvent);
        eventService.save(entityToSave);

        return eventMapper.toDomain(entityToSave);
    }

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

        if (isDifferentValue(updated.getName(), existing.getName())) {
            changes.add(buildChange("name", existing.getName(), updated.getName()));
        }

        if (isDifferentValue(updated.getMaxPlaces(), existing.getMaxPlaces())) {
            changes.add(buildChange("maxPlaces", existing.getMaxPlaces(), updated.getMaxPlaces()));
        }

        if (isDifferentValue(updated.getDate(), existing.getDate())) {
            changes.add(buildChange("date", existing.getDate(), updated.getDate()));
        }

        if (isDifferentValue(updated.getCost(), existing.getCost())) {
            changes.add(buildChange("cost", existing.getCost(), updated.getCost()));
        }

        if (isDifferentValue(updated.getDuration(), existing.getDuration())) {
            changes.add(buildChange("duration", existing.getDuration(), updated.getDuration()));
        }

        Long oldLocationId = existing.getLocation() != null ? existing.getLocation().getId() : null;
        Long newLocationId = updated.getLocation() != null ? updated.getLocation().getId() : null;
        if (isDifferentValue(oldLocationId, newLocationId)) {
            changes.add(buildChange("locationId", oldLocationId, newLocationId));
        }

        return changes;
    }

    private EventChange buildChange(String field, Object oldValue, Object newValue) {
        return EventChange.builder()
                .field(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }
}
