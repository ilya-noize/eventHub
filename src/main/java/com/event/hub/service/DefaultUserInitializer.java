package com.event.hub.service;

import com.event.hub.db.entity.UserRole;
import com.event.hub.model.user.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultUserInitializer {
    private final UserService userService;

    @PostConstruct
    public void init() {
        createUserIfNotExists(
                User.builder()
                        .login("admin")
                        .password("admin")
                        .age(99)
                        .role(UserRole.ADMIN.name())
                        .build()
        );
        createUserIfNotExists(
                User.builder()
                        .login("user")
                        .password("user")
                        .age(18)
                        .role(UserRole.USER.name())
                        .build()
        );
    }

    private void createUserIfNotExists(User user) {
        if (userService.isUserExistsByLogin(user.login())) {
            return;
        }
        userService.registrationUser(user);
    }
}
