import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import FreelancerDashboard from '../pages/freelancer/FreelancerDashboard';
import { FreelancerPortalService } from '@/api/freelancerPortalService';

// Mock the API service
jest.mock('@/api/freelancerPortalService', () => ({
    FreelancerPortalService: {
        getAssignments: jest.fn(),
    },
}));

describe('FreelancerDashboard', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    const renderWithRouter = (ui: React.ReactElement) => {
        return render(<BrowserRouter>{ui}</BrowserRouter>);
    };

    it('shows loading state initially', () => {
        (FreelancerPortalService.getAssignments as jest.Mock).mockReturnValue(new Promise(() => { }));
        renderWithRouter(<FreelancerDashboard />);
        expect(screen.getByText(/Loading assignments/i)).toBeInTheDocument();
    });

    it('renders empty state when no projects exist', async () => {
        (FreelancerPortalService.getAssignments as jest.Mock).mockResolvedValue([]);
        renderWithRouter(<FreelancerDashboard />);

        await waitFor(() => {
            expect(screen.getByText(/No projects assigned yet/i)).toBeInTheDocument();
        });

        // Ensure KPIs are 0
        const kpis = screen.getAllByText('0');
        expect(kpis.length).toBeGreaterThanOrEqual(3);
    });

    it('renders assignments and correct KPI counts', async () => {
        const mockProjects = [
            { id: 1, name: 'Project Alpha', status: 'pending', deadline: '2026-12-31', startDate: '2026-01-01' },
            { id: 2, name: 'Project Beta', status: 'in progress', deadline: '2026-12-31', startDate: '2026-01-01' },
            { id: 3, name: 'Project Gamma', status: 'completed', deadline: '2026-12-31', startDate: '2026-01-01' }
        ];

        (FreelancerPortalService.getAssignments as jest.Mock).mockResolvedValue(mockProjects);
        renderWithRouter(<FreelancerDashboard />);

        await waitFor(() => {
            expect(screen.getByText('Project Alpha')).toBeInTheDocument();
            expect(screen.getByText('Project Beta')).toBeInTheDocument();
            expect(screen.getByText('Project Gamma')).toBeInTheDocument();
        });

        // 1 pending, 1 in progress, 1 completed = Each card should render '1'
        const kpis = screen.getAllByText('1');
        expect(kpis.length).toBeGreaterThanOrEqual(3);
    });
});
