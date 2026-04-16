package com.auth.user;

import com.auth.api.UserRegistration;
import com.auth.domain.UserDto;
import com.event.common.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static com.event.common.tool.RandomResources.getRandomInteger;
import static com.event.common.tool.RandomResources.getRandomString;


public class UserResources {
    protected UserRegistration getUserRegistration() {
        return new UserRegistration(
                getRandomString(),
                getRandomString(),
                getRandomInteger()
        );
    }

    protected UserDto getAdminUserDto() {
        return UserDto.builder()
                .login("admin" + getRandomString())
                .password("admin")
                .age(99)
                .roles(List.of(new SimpleGrantedAuthority(UserRole.ADMIN.name())))
                .build();
    }

    protected UserDto getSimpleUserDTO() {
        return UserDto.builder()
                .login("user" + getRandomString())
                .password("user")
                .age(18)
                .roles(List.of(new SimpleGrantedAuthority(UserRole.USER.name())))
                .build();
    }

    protected UserDto getUserDTO() {
        return UserDto.builder()
                .login(getRandomString())
                .age(getRandomInteger())
                .roles(List.of(new SimpleGrantedAuthority(UserRole.USER.name())))
                .build();
    }
}