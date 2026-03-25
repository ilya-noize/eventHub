package com.event.hub.security;

import com.event.hub.model.user.User;
import com.event.hub.model.user.UserCredentials;
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

    public String authenticateUser(UserCredentials credentials) {
        var authentication = new UsernamePasswordAuthenticationToken(
                credentials.login(),
                credentials.password()
        );
        authenticationManager.authenticate(authentication);

        return jwtTokenManager.generateToken(credentials.login());
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional.ofNullable(authentication).orElseThrow(
                () -> new IllegalStateException("Auth not present")
        );
        return (User) authentication.getPrincipal();
    }

    public Long getCurrentAuthenticatedUserId() {
        return getCurrentAuthenticatedUser().getId();
    }

    public void verifyAuthenticatedUserAsOwnerResource(Long ownerId) {
        Long authUserId = getCurrentAuthenticatedUserId();
        if (!ownerId.equals(authUserId)) {
            throw new SecurityException("The owner of the resource is another user");
        }
    }
}
