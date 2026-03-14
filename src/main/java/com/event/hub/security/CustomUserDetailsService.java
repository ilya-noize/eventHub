package com.event.hub.security;

import com.event.hub.db.UserRepository;
import com.event.hub.db.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByLogin(username).orElseThrow(
                () -> new UsernameNotFoundException("Username not found: %s".formatted(username))
        );
        log.info("login={}. password={}.",username, user.getPassword());
        return User.withUsername(username)
                .password(user.getPassword())
                .authorities(user.getRole())
                .roles(user.getRole())
                .build();
    }
}
