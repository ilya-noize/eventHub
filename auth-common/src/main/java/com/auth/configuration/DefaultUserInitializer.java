package com.auth.configuration;

import com.auth.domain.UserDto;
import com.auth.domain.UserService;
import com.event.common.UserRole;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
                UserDto.builder()
                        .login("admin")
                        .password("admin")
                        .age(99)
                        .roles(List.of(new SimpleGrantedAuthority(UserRole.ADMIN.name())))
                        .build()
        );
        createUserIfNotExists(
                UserDto.builder()
                        .login("user")
                        .password("user")
                        .age(18)
                        .roles(List.of(new SimpleGrantedAuthority(UserRole.USER.name())))
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
                List<UserDto> userDtos = mapper.readValue(inputStream,
                        mapper.getTypeFactory().constructCollectionType(List.class, UserDto.class));
                userDtos.forEach(this::createUserIfNotExists);
            }
        } catch (IOException e) {
            throw new RuntimeException("File " + userJson + " not found:" + e.getMessage(), e);
        }
    }

    private void createUserIfNotExists(UserDto userDto) {
        if (userService.isUserExistsByLogin(userDto.getUsername())) {
            return;
        }
        userService.registrationUser(userDto);
    }
}
