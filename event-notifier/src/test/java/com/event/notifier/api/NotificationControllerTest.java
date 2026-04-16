package com.event.notifier.api;

import com.event.notifier.domain.EventNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private EventNotificationService eventNotificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    void getNotifications_success_returnsListOfNotifications() throws Exception {
        Long eventId = 1L;
        OffsetDateTime now = OffsetDateTime.now();
        List<NotificationResponse> expected = List.of(
                NotificationResponse.builder()
                        .notificationId(1L)
                        .type("CREATE")
                        .eventId(eventId)
                        .createdAt(now.toLocalDateTime())
                        .isRead(false)
                        .message("Вы создали новое событие!")
                        .payload("[{\"field\":\"title\",\"oldValue\":\"Old\",\"newValue\":\"New\"}]")
                        .build()
        );

        when(eventNotificationService.getNotifications()).thenReturn(expected);

        mockMvc.perform(get("/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].notificationId").value(1))
                .andExpect(jsonPath("$[0].type").value("CREATE"))
                .andExpect(jsonPath("$[0].eventId").value(eventId.toString()))
                .andExpect(jsonPath("$[0].isRead").value(false))
                .andExpect(jsonPath("$[0].message").value("Вы создали новое событие!"));
    }

    @Test
    void markNotificationAsRead_validIds_marksAsRead() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        MarkNotificationAsReadRequest request = new MarkNotificationAsReadRequest(ids);

        doNothing().when(eventNotificationService).markNotificationAsRead(ids);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getNotifications_emptyList_returnsEmptyArray() throws Exception {
        when(eventNotificationService.getNotifications()).thenReturn(List.of());

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void markNotificationAsRead_singleId_callsService() throws Exception {
        List<Long> ids = List.of(42L);
        MarkNotificationAsReadRequest request = new MarkNotificationAsReadRequest(ids);

        doNothing().when(eventNotificationService).markNotificationAsRead(ids);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getNotifications_multipleItems_returnsAllItems() throws Exception {
        Long event1 = 1L;
        Long event2 = 2L;
        List<NotificationResponse> expected = List.of(
                NotificationResponse.builder()
                        .notificationId(1L).type("UPDATE").eventId(event1).isRead(true).build(),
                NotificationResponse.builder()
                        .notificationId(2L).type("DELETE").eventId(event2).isRead(false).build()
        );

        when(eventNotificationService.getNotifications()).thenReturn(expected);

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notificationId").value(1))
                .andExpect(jsonPath("$[1].notificationId").value(2));
    }
}