package com.event.notifier.api;

import com.event.notifier.domain.EventNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final EventNotificationService eventNotificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public void markNotificationAsRead(@RequestBody MarkNotificationAsReadRequest request) {
        log.debug("POST /notifications called");
        eventNotificationService.markNotificationAsRead(request.notificationIds());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public List<NotificationResponse> getNotifications() {
        log.debug("GET /notifications called");
        return eventNotificationService.getNotifications();
    }
}
