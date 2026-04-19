/*
package com.event.manager;

import com.auth.security.AuthorizationService;
import com.event.manager.api.event.EventPostRequest;
import com.event.manager.configuration.DefaultLocationInitializer;
import com.event.manager.domain.EventMapper;
import com.event.manager.domain.event.EventManager;
import com.event.manager.domain.location.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@Disabled
public class EventControllerSecurityTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("event_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    WebApplicationContext context;

    @Autowired
    EventManager eventManager;

    @Autowired
    EventMapper eventMapper;

    @Autowired
    LocationService locationService;

    @Autowired
    DefaultLocationInitializer locationInitializer = new DefaultLocationInitializer(locationService);

    @MockitoBean
    AuthorizationService authorizationService; // ← мокаем сервис получения текущего пользователя

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long TEST_USER_ID = 100L;

    @BeforeEach
    void setUp() {
//        locationInitializer.init();

        // Настраиваем MockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();

        // Любой @WithMockUser будет иметь ID = 100
        when(authorizationService.getCurrentAuthorizedUserId()).thenReturn(TEST_USER_ID);
    }

    // ========================================================
    // ТЕСТ: Создание события от имени мокнутого пользователя
    // ========================================================

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldCreateEvent_WhenUserIsAuthenticated() throws Exception {
        EventPostRequest request = EventPostRequest.builder()
                .name("Concert in the Park")
                .maxPlaces(50)
                .date(LocalDateTime.now().plusDays(7))
                .cost(BigDecimal.valueOf(30))
                .duration(120)
                .locationId(1L)
                .build();

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Concert in the Park"))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID));
    }

    // ========================================================
    // ТЕСТ: Поиск событий
    // ========================================================

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldSearchEvents_ReturnResults() throws Exception {
        // Создаём событие через сервис (уже с owner = 100)
        eventManager.createEvent(eventMapper.toDomain(EventPostRequest.builder()
                .name("Festival 2025")
                .maxPlaces(100)
                .date(LocalDateTime.now().plusDays(10))
                .cost(BigDecimal.valueOf(100))
                .duration(180)
                .locationId(1L)
                .build()));

        mockMvc.perform(post("/events/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content[0].name").value("Festival 2025"));
    }

    // ========================================================
    // ТЕСТ: Доступ к своим событиям
    // ========================================================

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldGetMyEvents_ReturnOwnedEvents() throws Exception {
        // Убедимся, что событие создано от имени текущего пользователя
        eventManager.createEvent(eventMapper.toDomain(EventPostRequest.builder()
                .name("My Private Workshop")
                .maxPlaces(10)
                .date(LocalDateTime.now().plusDays(3))
                .cost(BigDecimal.ZERO)
                .duration(60)
                .locationId(1L)
                .build()));

        mockMvc.perform(get("/events/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("My Private Workshop"));
    }

    // ========================================================
    // ТЕСТ: Доступ запрещён — неавторизованный пользователь
    // ========================================================

    @Test
    void shouldReturnForbidden_WhenUserNotAuthenticated() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()) // Перенаправление на /login (если security активна)
                .andExpect(header().string("Location", "/login")); // или 401, зависит от настроек
    }
}*/
