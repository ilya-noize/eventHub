package com.event.hub.controller;

import com.event.hub.filter.EventSearchFilter;
import com.event.hub.filter.PageableFilter;
import com.event.hub.model.event.Event;
import com.event.hub.model.event.EventMapper;
import com.event.hub.model.event.EventPostRequest;
import com.event.hub.model.event.EventPutRequest;
import com.event.hub.model.event.EventResponse;
import com.event.hub.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @PostMapping("/tour")
    public List<EventResponse> createEvent(
            @RequestBody @Valid List<EventPostRequest> request
    ) {
        log.info("Creating tour of {} events", request.size());
        List<Event> events = request.stream()
                .map(eventMapper::toDomain)
                .toList();
        return eventService.createEventTour(events).stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @PostMapping
    public EventResponse createEvent(
            @RequestBody @Valid EventPostRequest request
    ) {
        log.info("Creating new event: {}", request.toString());
        Event event = eventMapper.toDomain(request);
        return eventMapper.toResponse(eventService.createEvent(event));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        log.info("Deleting event by ID={}", id);
        eventService.deleteById(id);
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        log.info("Getting event with ID={}", id);
        return eventMapper.toResponse(eventService.getEventById(id));
    }

    @GetMapping("/my")
    public Page<EventResponse> getMyEvents(PageableFilter filter) {
        log.info("Getting my events");
        return eventService.getMyEvents(filter)
                .map(eventMapper::toResponse);
    }

    @PutMapping("/{id}")
    public EventResponse updateEventById(
            @PathVariable Long id,
            @RequestBody @Valid EventPutRequest request
    ) {
        log.info("Updating an existing event, ID={}", id);
        Event domain = eventMapper.toDomain(request);
        return eventMapper.toResponse(eventService.updateEventById(id, domain));
    }

    @PostMapping("/search")
    public Page<EventResponse> searchEvents(EventSearchFilter filter) {
        log.info("Searching for all available Events");
        return eventService.search(filter)
                .map(eventMapper::toResponse);
    }
}
