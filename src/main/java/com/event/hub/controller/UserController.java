package com.event.hub.controller;

import com.event.hub.model.user.JwtResponse;
import com.event.hub.model.user.User;
import com.event.hub.model.user.UserCredentials;
import com.event.hub.model.user.UserMapper;
import com.event.hub.model.user.UserRegistration;
import com.event.hub.model.user.UserResponse;
import com.event.hub.security.AuthenticationService;
import com.event.hub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registrationUser(
            @RequestBody @Valid UserRegistration registration
    ) {
        log.debug("Received a request to registration user:{}", registration.login());
        User domain = userMapper.toDomain(registration);

        return userMapper.toResponse(userService.registrationUser(domain));
    }

    @PostMapping("/auth")
    public JwtResponse authenticationUser(
            @RequestBody @Valid UserCredentials credentials
    ) {
        log.debug("Received a request to authentication user:{}", credentials.login());
        User domain = userMapper.toDomain(credentials);
        String token = authenticationService.authenticateUser(domain);

        return new JwtResponse(token);
    }
}
