package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.FinanceSummaryResponse;
import com.freelance.freelancepm.service.FinanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class FinanceControllerTest {

    @Mock
    private FinanceService financeService;

    @Mock
    private com.freelance.freelancepm.service.IManagerService managerService;

    @Mock
    private java.security.Principal principal;

    @InjectMocks
    private FinanceController controller;

    private FinanceSummaryResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = FinanceSummaryResponse.builder()
                .profitTrend(List.of(new FinanceSummaryResponse.ProfitTrendData("Jan", 5000)))
                .incomeExpense(List.of(new FinanceSummaryResponse.IncomeExpenseData("Jan", 10000, 5000)))
                .expenseBreakdown(List.of(new FinanceSummaryResponse.ExpenseBreakdownData("Tools", 1000, 20)))
                .build();
    }

    @Test
    void getFinanceSummary_ReturnsSummaryForDefaultPeriod() {
        when(principal.getName()).thenReturn("test@test.com");
        when(managerService.getManagerIdByEmail("test@test.com")).thenReturn(1);
        when(financeService.getFinanceSummary(1, "month")).thenReturn(mockResponse);

        ResponseEntity<FinanceSummaryResponse> response = controller.getFinanceSummary(principal, "month");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getProfitTrend().size());
        verify(financeService).getFinanceSummary(1, "month");
    }

    @Test
    void getFinanceSummary_ReturnsSummaryForWeek() {
        when(principal.getName()).thenReturn("test@test.com");
        when(managerService.getManagerIdByEmail("test@test.com")).thenReturn(1);
        when(financeService.getFinanceSummary(1, "week")).thenReturn(mockResponse);

        ResponseEntity<FinanceSummaryResponse> response = controller.getFinanceSummary(principal, "week");

        assertEquals(200, response.getStatusCode().value());
        verify(financeService).getFinanceSummary(1, "week");
    }
}
