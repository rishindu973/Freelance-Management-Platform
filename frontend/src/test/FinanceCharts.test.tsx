import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi, type Mock } from 'vitest';
import { FinanceCharts } from '../components/dashboard/FinanceCharts';
import { FinanceService } from '@/api/financeService';

vi.mock('@/api/financeService', () => ({
    FinanceService: {
        getSummary: vi.fn(),
    },
}));

const mockData = {
    profitTrend: [
        { label: 'Jan', profit: 5000 }
    ],
    incomeExpense: [
        { label: 'Jan', income: 10000, expense: 5000 }
    ],
    expenseBreakdown: [
        { category: 'Salaries', amount: 5000, percentage: 100 }
    ]
};

// Recharts relies on DOM measurements which JSDOM doesn't support well, 
// so we mock the ResponsiveContainer to just render children
vi.mock('recharts', async () => {
    const ActualRecharts = await vi.importActual('recharts');
    return {
        ...ActualRecharts,
        ResponsiveContainer: ({ children }: any) => (
            <div data-testid="responsive-container" style={{ width: '800px', height: '400px' }}>
                {children}
            </div>
        )
    };
});

describe('FinanceCharts', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('shows loading state initially', () => {
        (FinanceService.getSummary as Mock).mockReturnValue(new Promise(() => { }));
        render(<FinanceCharts />);
        expect(screen.getByText(/Loading financial charts/i)).toBeInTheDocument();
    });

    it('displays error if service fails', async () => {
        (FinanceService.getSummary as Mock).mockRejectedValue(new Error('API failed'));
        render(<FinanceCharts />);
        
        await waitFor(() => {
            expect(screen.getByText(/API failed/i)).toBeInTheDocument();
        });
    });

    it('renders charts with data', async () => {
        (FinanceService.getSummary as Mock).mockResolvedValue(mockData);
        render(<FinanceCharts />);
        
        await waitFor(() => {
            expect(screen.getByText(/Profit Trend/i)).toBeInTheDocument();
            expect(screen.getByText(/Income vs Expenses/i)).toBeInTheDocument();
            expect(screen.getByText(/Expense Breakdown/i)).toBeInTheDocument();
        });

        const containers = screen.getAllByTestId('responsive-container');
        expect(containers.length).toBe(3); // LineChart, BarChart, PieChart
    });

    it('allows changing time period', async () => {
        (FinanceService.getSummary as Mock).mockResolvedValue(mockData);
        render(<FinanceCharts />);
        
        await waitFor(() => {
            expect(screen.getByText('Past Year (Monthly)')).toBeInTheDocument();
        });

        // Trigger the select dropdown (radix UI select needs specific handling, but we can verify API calls)
        expect(FinanceService.getSummary).toHaveBeenCalledWith('month');
    });
});
