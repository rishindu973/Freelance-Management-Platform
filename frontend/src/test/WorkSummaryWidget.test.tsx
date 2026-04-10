import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi, type Mock } from 'vitest';
import { WorkSummaryWidget } from '../components/dashboard/WorkSummaryWidget';
import { WorkSummaryService } from '@/api/workSummaryService';

vi.mock('@/api/workSummaryService', () => ({
    WorkSummaryService: {
        getWorkSummary: vi.fn(),
    },
}));

const mockData = {
    completedThisMonth: 10,
    completedLastMonth: 5,
    completedGrowthPercentage: 100,
    completedProjectsThisMonth: [
        { id: 1, name: 'Project A', clientId: 1, deadline: '2026-04-01' }
    ],
    pendingThisMonth: 8,
    pendingLastMonth: 10,
    pendingGrowthPercentage: -20,
    pendingProjectsNearDeadline: [
        { id: 2, name: 'Project B', clientId: 2, deadline: '2026-04-15' }
    ]
};

describe('WorkSummaryWidget', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const renderWithRouter = (ui: React.ReactElement) => {
        return render(<BrowserRouter>{ui}</BrowserRouter>);
    };

    it('shows loading state initially', () => {
        (WorkSummaryService.getWorkSummary as Mock).mockReturnValue(new Promise(() => { }));
        renderWithRouter(<WorkSummaryWidget />);
        expect(screen.getByText(/Loading work summary/i)).toBeInTheDocument();
    });

    it('renders completed projects correctly', async () => {
        (WorkSummaryService.getWorkSummary as Mock).mockResolvedValue(mockData);
        renderWithRouter(<WorkSummaryWidget />);

        await waitFor(() => {
            expect(screen.getByText('10')).toBeInTheDocument(); // completedThisMonth
            expect(screen.getByText(/\+100% vs last month/i)).toBeInTheDocument(); // growth
            expect(screen.getByText('Project A')).toBeInTheDocument();
        });
    });
});
