package com.event.manager.domain.event;

import com.event.manager.db.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventValidationService {
    private final EventRepository eventRepository;

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

    /**
     * Проверка на то что количество мест не меньше занятых
     * @see EventManager#updateEventById(Long, EventDto)
     */
    public void validateMaxAndOccupiedPlaces(Integer maxPlaces, Integer occupiedPlaces) {
        if (maxPlaces.compareTo(occupiedPlaces) < 0) {
            throw new IllegalArgumentException("Max Places can't be less then occupied");
        }
    }
}
