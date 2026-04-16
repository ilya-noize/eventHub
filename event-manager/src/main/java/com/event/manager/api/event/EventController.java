package com.event.manager.api.event;

import com.event.manager.domain.EventMapper;
import com.event.manager.domain.event.EventDto;
import com.event.manager.domain.event.EventManager;
import com.event.manager.filter.EventSearchFilter;
import com.event.manager.filter.PageableFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final EventManager eventManager;
    private final EventMapper eventMapper;

    @PostMapping("/tour")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public List<EventResponse> createTourEvents(@RequestBody @Valid List<EventPostRequest> request) {
        log.info("Creating tour of {} events", request.size());
        List<EventDto> events = request.stream()
                .map(eventMapper::toDomain)
                .toList();
        return eventManager.createTourEvents(events).stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public EventResponse createEvent(@RequestBody @Valid EventPostRequest request) {
        log.info("Creating new event: {}", request.toString());
        EventDto dto = eventManager.createEvent(eventMapper.toDomain(request));

        return eventMapper.toResponse(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@eventManager.isEventOwnerOrAdmin(#id)")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public void deleteById(@PathVariable Long id) {
        log.info("Deleting event by ID={}", id);
        eventManager.deleteById(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public EventResponse getEventById(@PathVariable Long id) {
        log.info("Getting event with ID={}", id);
        EventDto dto = eventManager.getEventById(id);

        return eventMapper.toResponse(dto);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Page<EventResponse> getMyEvents(PageableFilter filter) {
        log.info("Getting my events");

        return eventManager.getMyEvents(filter).map(eventMapper::toResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@eventManager.isEventOwnerOrAdmin(#id)")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public EventResponse updateEventById(
            @PathVariable Long id,
            @RequestBody @Valid EventPutRequest request
    ) {
        log.info("Updating an existing event, ID={}", id);
        EventDto dto = eventManager.updateEventById(id, eventMapper.toDomain(request));

        return eventMapper.toResponse(dto);
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Page<EventResponse> searchEvents(EventSearchFilter filter) {
        log.info("Searching for all available Events");

        return eventManager.search(filter).map(eventMapper::toResponse);
    }
}
