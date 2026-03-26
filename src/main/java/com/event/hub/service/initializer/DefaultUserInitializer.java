package com.event.hub.service.initializer;

import com.event.hub.db.entity.UserRole;
import com.event.hub.model.user.User;
import com.event.hub.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
        createFiftyUserFromJson();
    }

    private void createFiftyUserFromJson() {
        String userJson = "user.json";
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource(userJson);
            try (InputStream inputStream = resource.getInputStream()) {
                List<User> users = mapper.readValue(inputStream,
                        mapper.getTypeFactory().constructCollectionType(List.class, User.class));
                users.forEach(this::createUserIfNotExists);
            }
        } catch (IOException e) {
            throw new RuntimeException("File " + userJson + " not found:" + e.getMessage(), e);
        }
    }

    private void createUserIfNotExists(User user) {
        if (userService.isUserExistsByLogin(user.getUsername())) {
            return;
        }
        userService.registrationUser(user);
    }
}
