package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ActivityResponse;
import com.freelance.freelancepm.entity.Activity;
import com.freelance.freelancepm.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

    @Test
    void testGetActivities_PaginationAndFetchAll() throws Exception {
        ActivityResponse act = ActivityResponse.builder()
                .id(1L)
                .managerId(1L)
                .type(Activity.ActivityType.PROJECT_CREATED)
                .description("Project XYZ")
                .build();

        Page<ActivityResponse> mockPage = new PageImpl<>(List.of(act));
        when(activityService.list(eq(1L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/activities")
                        .header("X-Manager-Id", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Project XYZ"));
    }

    @Test
    void testGetActivities_FilterByType() throws Exception {
        Page<ActivityResponse> mockPage = new PageImpl<>(List.of());
        when(activityService.list(eq(1L), eq(Activity.ActivityType.INVOICE_SENT), any(), any(), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/activities")
                        .header("X-Manager-Id", "1")
                        .param("type", "INVOICE_SENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetActivities_FilterByDateRange() throws Exception {
        Page<ActivityResponse> mockPage = new PageImpl<>(List.of());
        when(activityService.list(eq(1L), any(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/activities")
                        .header("X-Manager-Id", "1")
                        .param("startDate", "2023-01-01T00:00:00")
                        .param("endDate", "2023-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
