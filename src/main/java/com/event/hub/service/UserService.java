package com.event.hub.service;

import com.event.hub.db.UserRepository;
import com.event.hub.db.entity.UserEntity;
import com.event.hub.db.entity.UserRole;
import com.event.hub.model.user.User;
import com.event.hub.model.user.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public User registrationUser(User domain) {
        if (isUserExistsByLogin(domain.login())) {
            throw new ResponseStatusException(HttpStatus.valueOf(400));
            //"Login already taken");
        }
        UserEntity entity = userMapper.toEntity(domain);
        if (entity.getRole() == null) entity.setRole(UserRole.USER.name());
        entity.setPassword(passwordEncoder.encode(domain.password()));
        userRepository.save(entity);

        return userMapper.toDomain(entity);
    }

    public boolean isUserExistsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    public User getUserById(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such User ID=%s".formatted(id))
        );
        return userMapper.toDomain(user);
    }
}
