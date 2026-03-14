package com.event.hub.security;

import com.event.hub.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;

    public String authenticateUser(User domain) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        domain.login(),
                        domain.password()
                )
        );
        return jwtTokenManager.generateToken(domain.login());
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional.ofNullable(authentication).orElseThrow(
                () -> new IllegalStateException("Auth not present")
        );
        return (User) authentication.getPrincipal();
    }
}
