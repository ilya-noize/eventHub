package com.event.hub.service;

import com.event.hub.db.UserRepository;
import com.event.hub.db.entity.UserEntity;
import com.event.hub.db.entity.UserRole;
import com.event.hub.model.user.User;
import com.event.hub.model.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public User registrationUser(User domain) {
        if (isUserExistsByLogin(domain.login())) {
            throw new IllegalArgumentException();
        }
        UserEntity entity = userMapper.toEntity(domain);
        entity.setRole(UserRole.USER.name());
        entity.setPassword(passwordEncoder.encode(domain.password()));
        userRepository.save(entity);

        return userMapper.toDomain(entity);
    }

    public boolean isUserExistsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }
}
