package com.event.manager.domain.registration;

import com.event.manager.db.EventEntity;
import com.event.manager.db.EventRegistrationEntity;
import com.event.manager.db.EventRegistrationRepository;
import com.event.manager.db.EventRepository;
import com.event.manager.domain.EventMapper;
import com.event.manager.domain.event.EventDto;
import com.event.manager.filter.PageableFilter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventRegistrationService {
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventMapper eventMapper;

    public void saveRegistrationEvent(EventRegistrationEntity registration) {
        if (eventRepository.occupyEmptyPlace(registration.getEvent().getId()) == 0) {
            throw new IllegalStateException("Registered to his event is failed");
        }
        eventRegistrationRepository.save(registration);
    }

    public void cancelRegistrationEvent(Long userId, EventEntity event) {
        var registration = eventRegistrationRepository.findByUserIdAndEvent(userId, event)
                .orElseThrow(() -> new EntityNotFoundException(
                        "You are not registered in this event ID=%s".formatted(event.getId())
                ));
        eventRepository.freeUpOccupiedSpace(registration.getEvent().getId());
        eventRegistrationRepository.delete(registration);
    }

    public Page<EventDto> getRegistrationsEvent(Long userId, PageableFilter filter) {
        Page<EventDto> pages = eventRegistrationRepository
                .findAllEventWithRegistrations(userId, filter.toPageable())
                .map(eventMapper::toDomain);
        return pages.isEmpty() ? Page.empty() : pages;
    }
}
