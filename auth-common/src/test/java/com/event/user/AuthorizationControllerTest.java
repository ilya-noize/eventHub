package com.event.user;

import com.event.api.JwtResponse;
import com.event.api.UserCredentials;
import com.event.api.UserResponse;
import com.event.common.exception.ErrorResponse;
import com.event.domain.UserDto;
import com.event.domain.UserService;
import com.event.security.AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizationControllerTest extends UserResources {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void successfulGetUserByIdWhenAuthenticatedAsAdmin() throws Exception {
        UserDto userDto = getAdminUserDto();
        UserDto saved = userService.registrationUser(userDto);
        UserCredentials credentials = new UserCredentials(userDto.getUsername(), userDto.getPassword());
        JwtResponse jwtResponse = authenticationService.authenticateUser(credentials);

        String responseJson = mockMvc.perform(get("/users/{id}", saved.getId())
                        .header("Authorization", "Bearer " + jwtResponse.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserResponse expectedResponseBody = mapper.readValue(responseJson, UserResponse.class);

        Assertions.assertEquals(saved.getId(), expectedResponseBody.id());
        Assertions.assertEquals(userDto.getLogin(), expectedResponseBody.login());
        Assertions.assertEquals(userDto.getAge(), expectedResponseBody.age());
        Assertions.assertEquals(userDto.getRoles().toString(), expectedResponseBody.role());
    }


    @Test
    void shouldForbidden403WhenAuthenticateAsUser() throws Exception {
        UserDto userDto = getSimpleUserDTO();
        UserDto saved = userService.registrationUser(userDto);
        long userId = saved.getId();
        JwtResponse jwtResponse = authenticationService.authenticateUser(new UserCredentials(
                userDto.getUsername(),
                userDto.getPassword()
        ));

        String responseJson = mockMvc.perform(get("/users/{id}", userId)
                        .header("Authorization", "Bearer " + jwtResponse.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ErrorResponse expectedResponseBody = mapper.readValue(responseJson, ErrorResponse.class);

        Assertions.assertEquals("Insufficient permissions to perform", expectedResponseBody.message());
        Assertions.assertEquals("Access Denied", expectedResponseBody.detailedMessage());
    }
}