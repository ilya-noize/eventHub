package com.event.domain;

import com.event.db.UserEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserMapper {

    public UserDto toDomain(UserEntity entity) {
        return UserDto.builder()
                .id(entity.getId())
                .login(entity.getLogin())
                .password(entity.getPassword())
                .age(entity.getAge())
                .roles(grantedAuthorities(entity.getRole()))
                .build();
    }

    private List<SimpleGrantedAuthority> grantedAuthorities(String role) {
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        return Collections.singletonList(authority);
    }
}
