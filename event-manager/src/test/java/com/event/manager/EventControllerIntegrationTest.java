/*
package com.event.manager;

import com.event.manager.domain.event.EventDto;
import com.event.manager.domain.EventMapper;
import com.event.manager.domain.event.EventManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = EventControllerIntegrationTest.Initializer.class)
@Testcontainers
@Disabled
public class EventControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("event_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    WebApplicationContext context;

    @Autowired
    EventManager eventManager;

    @Autowired
    EventMapper eventMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword()
            ).applyTo(context.getEnvironment());
        }
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateEvent_ReturnCreatedEvent() throws Exception {
        EventPostRequest request = EventPostRequest.builder()
                .name("Test Event")
                .maxPlaces(10)
                .date(LocalDateTime.now().plusDays(1))
                .cost(BigDecimal.valueOf(50))
                .duration(60)
                .locationId(1L)
                .build();

        mockMvc.perform(post("/events")
                        .contentType("application/json")
                        .header("Authorization", "Bearer dummy-jwt")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Event"))
                .andExpect(jsonPath("$.maxPlaces").value(10));
    }

    @Test
    void shouldSearchEvents_ReturnPageOfEvents() throws Exception {
        // Сначала создадим событие через сервис (т.к. location должна существовать)
        EventDto created = eventManager.createEvent(eventMapper.toDomain(EventPostRequest.builder()
                .name("Search Test")
                .maxPlaces(5)
                .date(LocalDateTime.now().plusDays(2))
                .cost(BigDecimal.valueOf(25))
                .duration(90)
                .locationId(1L)
                .build()));

        mockMvc.perform(post("/events/search")
                        .contentType("application/json")
                        .header("Authorization", "Bearer dummy-jwt")
                        .content("{}")) // пустой фильтр
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").value(hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Search Test"));
    }
}*/
