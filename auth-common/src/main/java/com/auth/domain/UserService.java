package com.auth.domain;

import com.auth.db.UserEntity;
import com.auth.db.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserDto registrationUser(UserDto domain) {
        UserEntity entity = domain.toEntity();
        entity.setPassword(passwordEncoder.encode(domain.getPassword()));
        UserEntity save = userRepository.save(entity);
        return userMapper.toDomain(save);
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("No such User ID=%s".formatted(id)));
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException("Username not found: %s".formatted(username)));
    }

    public boolean isUserExistsByLogin(String username) {
        return userRepository.existsByLogin(username);
    }
}
