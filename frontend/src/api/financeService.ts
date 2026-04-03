import { apiClient } from "./axiosClient";

export interface ProfitTrendData {
    label: string;
    profit: number;
}

export interface IncomeExpenseData {
    label: string;
    income: number;
    expense: number;
}

export interface ExpenseBreakdownData {
    category: string;
    amount: number;
    percentage: number;
}

export interface FinanceSummaryResponse {
    profitTrend: ProfitTrendData[];
    incomeExpense: IncomeExpenseData[];
    expenseBreakdown: ExpenseBreakdownData[];
}

export const FinanceService = {
    getSummary: async (period: 'week' | 'month' | 'year' = 'month'): Promise<FinanceSummaryResponse> => {
        const response = await apiClient.get(`/api/manager/finance/summary?period=${period}`);
        return response.data;
    }
};
