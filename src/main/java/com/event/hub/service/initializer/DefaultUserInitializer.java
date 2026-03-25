package com.event.hub.service.initializer;

import com.event.hub.db.entity.UserRole;
import com.event.hub.model.user.User;
import com.event.hub.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
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
        if (userService.isUserExistsByLogin(user.getUsername())) {
            return;
        }
        userService.registrationUser(user);
    }
}
