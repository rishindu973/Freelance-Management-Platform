package com.freelance.freelancepm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSummaryResponse {
    private List<ProfitTrendData> profitTrend;
    private List<IncomeExpenseData> incomeExpense;
    private List<ExpenseBreakdownData> expenseBreakdown;

    @Data
    @AllArgsConstructor
    public static class ProfitTrendData {
        private String label;
        private double profit;
    }

    @Data
    @AllArgsConstructor
    public static class IncomeExpenseData {
        private String label;
        private double income;
        private double expense;
    }

    @Data
    @AllArgsConstructor
    public static class ExpenseBreakdownData {
        private String category;
        private double amount;
        private double percentage;
    }
}
