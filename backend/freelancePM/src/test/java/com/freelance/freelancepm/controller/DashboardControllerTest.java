package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.WorkSummaryResponse;
import com.freelance.freelancepm.service.IDashboardService;
import com.freelance.freelancepm.service.IManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

    @Mock
    private IDashboardService dashboardService;

    @Mock
    private IManagerService managerService;

    @Mock
    private Principal principal;

    @InjectMocks
    private DashboardController controller;

    private WorkSummaryResponse mockWorkSummary;

    @BeforeEach
    void setUp() {
        mockWorkSummary = WorkSummaryResponse.builder()
                .completedThisMonth(5)
                .completedLastMonth(2)
                .completedGrowthPercentage(150.0)
                .pendingThisMonth(10)
                .build();
    }

    @Test
    void getWorkSummary_ReturnsData() {
        when(principal.getName()).thenReturn("manager@test.com");
        when(managerService.getManagerIdByEmail("manager@test.com")).thenReturn(1);
        when(dashboardService.getWorkSummary(1)).thenReturn(mockWorkSummary);

        ResponseEntity<WorkSummaryResponse> response = controller.getWorkSummary(principal);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getCompletedThisMonth());
        assertEquals(150.0, response.getBody().getCompletedGrowthPercentage());
        verify(dashboardService).getWorkSummary(1);
    }
}
