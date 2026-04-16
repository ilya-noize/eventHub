package com.auth.api;

import com.auth.domain.UserDto;
import com.auth.domain.UserService;
import com.auth.security.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class AuthorizationController {
    private final AuthorizationService authorizationService;
    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public UserResponse getUser(@PathVariable Long id) {
        log.debug("Received a request to get user: ID={}", id);
        UserDto userDto = userService.getUserById(id);

        return userDto.toResponse();
    }

    @GetMapping("/my")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public UserResponse getUser() {
        log.debug("Received a request to get current user");
        UserDto userDto = authorizationService.getCurrentAuthorizedUser();

        return userDto.toResponse();
    }
}
