package com.event.manager.domain.event;

import com.event.manager.db.EventEntity;
import com.event.manager.db.EventRepository;
import com.event.manager.db.EventStatus;
import com.event.manager.domain.EventUpdateStatusScheduler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    @Transactional
    public EventEntity save(EventEntity entity) {
        return eventRepository.save(entity);
    }

    @Transactional
    public List<EventEntity> saveAll(List<EventEntity> entities) {
        return eventRepository.saveAll(entities);
    }

    @Transactional
    public void updateEventStatusToCanceled(Long id) {
        eventRepository.updateEventStatusToCanceled(id);
    }

    public EventEntity findById(Long id) {
        EventEntity event = eventRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such event. ID=%s".formatted(id))
        );
        log.info("Event found: {}", event.getName());
        return event;
    }

    /**
     * @see EventManager#deleteById(Long)
     * @see EventManager#updateEventById(Long, EventDto)
     * @return Событие с регистрациями которое еще не началось
     */
    public Optional<EventEntity> findByIdAndWaitStartWithRegistrations(Long eventId) {
        return eventRepository.findByIdWithRegistrations(eventId);
    }

    public Optional<EventEntity> findByIdAndWaitStart(Long eventId) {
        return eventRepository.findByIdAndStatus(eventId, EventStatus.WAIT_START.name());
    }

    public Page<EventEntity> findByOwner_Id(Long ownerId, Pageable pageable) {
        return eventRepository.findByOwner_Id(ownerId, pageable);
    }

    public Page<EventEntity> findAll(Specification<EventEntity> specification, Pageable pageable) {
        return eventRepository.findAll(specification, pageable);
    }

    /**
     * Updates all events statuses from 'WAITING_START', which are not 'STARTED' yet
     * @see EventUpdateStatusScheduler#updateAllEventsStatusToStarted()
     */
    @Transactional
    @Async
    public void updateAllEventsStatusToStarted() {
        int updated = eventRepository.updateAllEventsStatusToStarted();
        loggingResultsUpdateStatus(updated, EventStatus.STARTED.name());
    }

    /**
     * Updates all events statuses from 'STARTED', which are not 'FINISHED' yet
     * @see EventUpdateStatusScheduler#updateAllEventsStatusToFinished()
     */
    @Transactional
    @Async
    public void updateAllEventsStatusToFinished() {
        int updated = eventRepository.updateAllEventsStatusToFinished();
        loggingResultsUpdateStatus(updated, EventStatus.FINISHED.name());
    }

    private static void loggingResultsUpdateStatus(int updated, String status) {
        if (updated != 0) {
            log.info("{} Events status changed to {}", updated, status);
        }
        log.info("'updateAllEventsStatusTo{}' query returned no results", status);
    }

    public boolean checkIfDateIsFree(EventDto eventDto) {
        LocalDateTime dateStart = eventDto.getDate();
        LocalDateTime dateEnd = dateStart.plusMinutes(eventDto.getDuration());
        boolean isConflict = eventRepository.isTimeConflictBeforeReservation(
                eventDto.getLocationId(),
                dateStart,
                dateEnd
        );
        if (isConflict) {
            throw new IllegalArgumentException("There are other events taking place at the same time and in the same place");
        }
        return true;
    }
}
