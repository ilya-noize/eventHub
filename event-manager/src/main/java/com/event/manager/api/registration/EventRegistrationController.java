package com.event.manager.api.registration;

import com.event.manager.api.event.EventResponse;
import com.event.manager.domain.EventMapper;
import com.event.manager.domain.registration.EventRegistrationManager;
import com.event.manager.filter.PageableFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/registrations")
@RequiredArgsConstructor
@Slf4j
public class EventRegistrationController {
    private final EventRegistrationManager eventRegistrationManager;
    private final EventMapper eventMapper;

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Page<EventResponse> getMyRegistrationsEvent(PageableFilter filter) {
        log.info("get my registered events");
        return eventRegistrationManager.getMyRegistrationsEvent(filter)
                .map(eventMapper::toResponse);
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public void registrationEvent(@PathVariable Long id) {
        log.info("register to an event ID={}", id);
        eventRegistrationManager.registrationEvent(id);
    }

    @DeleteMapping("/cancel/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public void registrationCancelEvent(@PathVariable Long id) {
        log.info("delete a registered user from the list of participants in this meeting. Id = {}", id);
        eventRegistrationManager.cancelRegisteredUserFromTheListParticipantsInThisEventById(id);
    }
}
