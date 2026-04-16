package com.auth.user;

import com.auth.domain.UserDto;
import com.event.common.UserRole;
import com.event.common.exception.ErrorResponse;
import com.auth.api.JwtResponse;
import com.auth.api.UserCredentials;
import com.auth.api.UserRegistration;
import com.auth.api.UserResponse;
import com.auth.security.AuthenticationService;
import com.auth.domain.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static com.event.common.tool.RandomResources.getRandomInteger;
import static com.event.common.tool.RandomResources.getRandomString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest extends UserResources {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpServletRequest request = null;

    @Test
    void successfulRegistrationUserWithUniqueLogin() throws Exception {
        UserRegistration registrationRequest = getUserRegistration();

        String jsonRequest = mapper.writeValueAsString(registrationRequest);
        String jsonResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserResponse userResponse = mapper.readValue(jsonResponse, UserResponse.class);

        Assertions.assertNotNull(userResponse.id());
        Assertions.assertEquals(registrationRequest.login(), userResponse.login());
        Assertions.assertEquals(registrationRequest.age(), userResponse.age());
        Assertions.assertEquals(UserRole.USER.name(), userResponse.role());
    }

    @Test
    void shouldBadRequestIfTryRegisterDuplicateUserLogin() throws Exception {
        UserDto userDto = getUserDTO();
        userService.registrationUser(userDto);
        UserRegistration registrationRequest = new UserRegistration(
                userDto.getUsername(),
                getRandomString(),
                getRandomInteger()
        );
        String jsonRequest = mapper.writeValueAsString(registrationRequest);

        String jsonResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ErrorResponse expectedResponseBody = mapper.readValue(jsonResponse, ErrorResponse.class);

        Assertions.assertEquals("Client error", expectedResponseBody.message());
        Assertions.assertEquals("Login already taken", expectedResponseBody.detailedMessage());
    }

    @Test
    void shouldBadRequestIfTryRegisterEmptyUserLogin() throws Exception {
        UserRegistration registrationRequest = new UserRegistration(
                null,
                getRandomString(),
                getRandomInteger()
        );
        String jsonRequest = mapper.writeValueAsString(registrationRequest);

        String jsonResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ErrorResponse expectedResponseBody = mapper.readValue(jsonResponse, ErrorResponse.class);

        Assertions.assertEquals("Validate error", expectedResponseBody.message());
    }

    @Test
    void successfulAuthenticationUser() throws Exception {
        UserDto userDTO = getUserDTO();
        userService.registrationUser(userDTO);
        UserCredentials credentialsRequest = new UserCredentials(
                userDTO.getUsername(),
                userDTO.getPassword()
        );
        String jsonRequest = mapper.writeValueAsString(credentialsRequest);
        String jsonResponse = mockMvc.perform(post("/users/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JwtResponse jwtToken = mapper.readValue(jsonResponse, JwtResponse.class);

        Assertions.assertNotNull(jwtToken.accessToken());
    }

    @Test
    void shouldUnauthorized401WhenFailedAuthenticationUser() throws Exception {
        UserDto userDTO = getUserDTO();
        userService.registrationUser(userDTO);
        UserCredentials credentialsRequest = new UserCredentials(
                userDTO.getUsername(),
                getRandomString() // wrong password!
        );
        String jsonRequest = mapper.writeValueAsString(credentialsRequest);
        String jsonResponse = mockMvc.perform(post("/users/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                )
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ErrorResponse expectedResponseBody = mapper.readValue(jsonResponse, ErrorResponse.class);

        Assertions.assertEquals("Authentication failed", expectedResponseBody.message());
    }


    @Test
    void shouldUnauthorized401WhenNotFoundUserId() throws Exception {
        UserCredentials credentialsRequest = new UserCredentials(
                getRandomString(),
                getRandomString()
        );
        String jsonRequest = mapper.writeValueAsString(credentialsRequest);
        String jsonResponse = mockMvc.perform(post("/users/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                )
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ErrorResponse expectedResponseBody = mapper.readValue(jsonResponse, ErrorResponse.class);

        Assertions.assertEquals("Authentication failed", expectedResponseBody.message());
        Assertions.assertEquals("User not found", expectedResponseBody.detailedMessage());
    }
}