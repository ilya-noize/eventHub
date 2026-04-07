package com.event.hub.security;

import com.event.hub.db.UserRepository;
import com.event.hub.db.entity.UserEntity;
import com.event.hub.model.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByLogin(username).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );
        return new User(
                user.getId(),
                user.getLogin(),
                user.getPassword(),
                user.getAge(),
                user.getRole()
        );
    }
}
