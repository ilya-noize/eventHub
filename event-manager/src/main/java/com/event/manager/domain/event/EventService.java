package com.event.manager.domain.event;

import com.event.manager.db.EventEntity;
import com.event.manager.db.EventRepository;
import com.event.manager.db.EventStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateEventStatusToCanceledById(Long id) {
        eventRepository.updateStatusById(id, EventStatus.CANCELED.name());
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
}
