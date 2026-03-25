package com.event.hub.controller;

import com.event.hub.filter.PageableFilter;
import com.event.hub.model.event.EventMapper;
import com.event.hub.model.event.EventResponse;
import com.event.hub.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/registration")
@RequiredArgsConstructor
@Slf4j
public class EventRegistrationController {
    private final EventRegistrationService eventRegistrationService;
    private final EventMapper eventMapper;

    @GetMapping("/my")
    public Page<EventResponse> getMyRegistrationsEvent(PageableFilter filter) {
        log.info("get my registered events");
        return eventRegistrationService.getMyRegistrationsEvent(filter)
                .map(eventMapper::toResponse);
    }

    @PostMapping("/{id}")
    public void registrationEvent(@PathVariable Long id) {
        log.info("register to an event ID={}", id);
        eventRegistrationService.registrationEvent(id);
    }

    @DeleteMapping("/cancel/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registrationCancelEvent(@PathVariable Long id) {
        log.info("delete a registered user from the list of participants in this meeting. Id = {}", id);
        eventRegistrationService.cancelRegisteredUserFromTheListParticipantsInThisEventById(id);
    }
}
