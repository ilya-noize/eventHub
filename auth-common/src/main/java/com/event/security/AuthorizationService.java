package com.event.security;

import com.event.domain.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthorizationService {

    public UserDto getCurrentAuthorizedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional.ofNullable(authentication).orElseThrow(
                () -> new IllegalStateException("Authentication not present")
        );
        return (UserDto) authentication.getPrincipal();
    }

    public Long getCurrentAuthorizedUserId() {
        return this.getCurrentAuthorizedUser().getId();
    }
}
