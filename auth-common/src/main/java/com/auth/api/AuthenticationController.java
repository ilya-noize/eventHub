package com.auth.api;

import com.auth.domain.UserDto;
import com.auth.domain.UserService;
import com.auth.security.AuthenticationService;
import com.auth.security.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RateLimitService rateLimitService;
    private final UserService userService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registrationUser(
            @RequestBody @Valid UserRegistration registration
    ) {
        String login = registration.login();
        log.debug("Received a request to registration user:{}", login);

        if (userService.isUserExistsByLogin(login)) {
            throw new IllegalArgumentException("Login already taken");
        }
        UserDto dto = userService.registrationUser(registration.mappingDto());
        return dto.toResponse();
    }

    @PostMapping("/auth")
    public JwtResponse authenticationUser(
            @RequestBody @Valid UserCredentials credentials,
            HttpServletRequest request
    ) {
        log.debug("Received a request to authentication user:{}", credentials.login());
        rateLimitService.validateFiveRequestsOneMinute(request);
        return authenticationService.authenticateUser(credentials);
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    public JwtResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {
        log.debug("Received a request to refresh token");
        return authenticationService.refreshToken(request.refreshToken());
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    public void logout() {
        authenticationService.logout();
    }
}
