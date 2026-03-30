package com.event.hub.service;

import com.event.hub.db.EventRepository;
import com.event.hub.db.entity.EventEntity;
import com.event.hub.db.entity.EventStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        log.info("Event found: {}", event.toString());
        return event;
    }

    public Page<EventEntity> findByOwner_Id(Long ownerId, Pageable pageable) {
        return eventRepository.findByOwner_Id(ownerId, pageable);
    }

    public Page<EventEntity> findAll(Specification<EventEntity> specification, Pageable pageable) {
        return eventRepository.findAll(specification, pageable);
    }

    /**
     * Updates all events statuses from 'WAITING_START', which are not 'STARTED' yet
     */
    @Transactional
    @Async
    public void updateAllEventsStatusToStarted() {
        int updated = eventRepository.updateAllEventsStatusToStarted();
        loggingResultsUpdateStatus(updated, EventStatus.STARTED.name());
    }

    /**
     * Updates all events statuses from 'STARTED', which are not 'FINISHED' yet
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
        log.debug("'updateAllEventsStatusTo{}' query returned no results", status);
    }
}
