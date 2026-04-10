package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.FinanceSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceService {

    public FinanceSummaryResponse getFinanceSummary(String period) {
        List<FinanceSummaryResponse.ProfitTrendData> profitTrend = new ArrayList<>();
        List<FinanceSummaryResponse.IncomeExpenseData> incomeExpense = new ArrayList<>();
        List<FinanceSummaryResponse.ExpenseBreakdownData> expenseBreakdown = new ArrayList<>();

        if ("week".equalsIgnoreCase(period)) {
            // Mock data for week
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            double[] incomes = {1200, 1500, 800, 2200, 3100, 400, 100};
            double[] expenses = {400, 300, 200, 500, 600, 100, 50};
            for (int i = 0; i < 7; i++) {
                double profit = incomes[i] - expenses[i];
                incomeExpense.add(new FinanceSummaryResponse.IncomeExpenseData(days[i], incomes[i], expenses[i]));
                profitTrend.add(new FinanceSummaryResponse.ProfitTrendData(days[i], profit));
            }
        } else if ("year".equalsIgnoreCase(period)) {
            // Mock data for year
            String[] years = {"2022", "2023", "2024", "2025", "2026"};
            double[] incomes = {120000, 145000, 160000, 180000, 210000};
            double[] expenses = {80000, 95000, 100000, 115000, 120000};
            for (int i = 0; i < 5; i++) {
                double profit = incomes[i] - expenses[i];
                incomeExpense.add(new FinanceSummaryResponse.IncomeExpenseData(years[i], incomes[i], expenses[i]));
                profitTrend.add(new FinanceSummaryResponse.ProfitTrendData(years[i], profit));
            }
        } else {
            // Default "month"
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            double[] incomes = {10000, 12000, 15000, 13000, 14000, 16000, 18000, 17500, 19000, 21000, 20500, 25000};
            double[] expenses = {5000, 5500, 6000, 5800, 5900, 6200, 6500, 6300, 7000, 7200, 7500, 8000};
            for (int i = 0; i < 12; i++) {
                double profit = incomes[i] - expenses[i];
                incomeExpense.add(new FinanceSummaryResponse.IncomeExpenseData(months[i], incomes[i], expenses[i]));
                profitTrend.add(new FinanceSummaryResponse.ProfitTrendData(months[i], profit));
            }
        }

        // Generate Expense Breakdown (constant percentages for simplicity)
        double totalExpense = incomeExpense.stream().mapToDouble(FinanceSummaryResponse.IncomeExpenseData::getExpense).sum();
        
        expenseBreakdown.add(new FinanceSummaryResponse.ExpenseBreakdownData("Freelancer Salaries", totalExpense * 0.50, 50));
        expenseBreakdown.add(new FinanceSummaryResponse.ExpenseBreakdownData("Software Tools", totalExpense * 0.20, 20));
        expenseBreakdown.add(new FinanceSummaryResponse.ExpenseBreakdownData("Marketing", totalExpense * 0.15, 15));
        expenseBreakdown.add(new FinanceSummaryResponse.ExpenseBreakdownData("Office & Admin", totalExpense * 0.10, 10));
        expenseBreakdown.add(new FinanceSummaryResponse.ExpenseBreakdownData("Misc", totalExpense * 0.05, 5));

        return FinanceSummaryResponse.builder()
                .profitTrend(profitTrend)
                .incomeExpense(incomeExpense)
                .expenseBreakdown(expenseBreakdown)
                .build();
    }
}
