import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi, type Mock } from 'vitest';
import FreelancerProjects from '../pages/freelancer/FreelancerProjects';
import { FreelancerPortalService } from '@/api/freelancerPortalService';

vi.mock('@/api/freelancerPortalService', () => ({
    FreelancerPortalService: {
        getAssignments: vi.fn(),
        getProfile: vi.fn(),
        updateAvailability: vi.fn(),
    },
}));

const mockProjects = [
    {
        id: 1,
        name: 'Website Redesign',
        status: 'in progress',
        deadline: '2026-06-15',
        startDate: '2026-01-01',
        clientName: 'Acme Corp',
        description: 'Redesign client website',
        type: 'web',
        clientId: 1,
        managerId: 1,
        team: [],
    },
    {
        id: 2,
        name: 'Mobile App',
        status: 'pending',
        deadline: '2026-08-01',
        startDate: '2026-03-01',
        clientName: 'Tech Inc',
        description: 'Build mobile application',
        type: 'mobile',
        clientId: 2,
        managerId: 1,
        team: [],
    },
];

describe('FreelancerProjects', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const renderWithRouter = (ui: React.ReactElement) => {
        return render(<BrowserRouter>{ui}</BrowserRouter>);
    };

    it('shows loading state initially', () => {
        (FreelancerPortalService.getAssignments as Mock).mockReturnValue(new Promise(() => { }));
        renderWithRouter(<FreelancerProjects />);
        expect(screen.getByText(/Loading projects/i)).toBeInTheDocument();
    });

    it('renders empty state when no projects', async () => {
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue([]);
        renderWithRouter(<FreelancerProjects />);

        await waitFor(() => {
            expect(screen.getByText(/No projects found/i)).toBeInTheDocument();
        });
    });

    it('renders project list with client names', async () => {
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRouter(<FreelancerProjects />);

        await waitFor(() => {
            expect(screen.getByText('Website Redesign')).toBeInTheDocument();
            expect(screen.getByText('Mobile App')).toBeInTheDocument();
            expect(screen.getByText(/Acme Corp/)).toBeInTheDocument();
            expect(screen.getByText(/Tech Inc/)).toBeInTheDocument();
        });
    });

    it('filters projects by search term', async () => {
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRouter(<FreelancerProjects />);

        await waitFor(() => {
            expect(screen.getByText('Website Redesign')).toBeInTheDocument();
        });

        const searchInput = screen.getByPlaceholderText('Search projects...');
        fireEvent.change(searchInput, { target: { value: 'Mobile' } });

        await waitFor(() => {
            expect(screen.queryByText('Website Redesign')).not.toBeInTheDocument();
            expect(screen.getByText('Mobile App')).toBeInTheDocument();
        });
    });

    it('filters projects by status', async () => {
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRouter(<FreelancerProjects />);

        await waitFor(() => {
            expect(screen.getByText('Website Redesign')).toBeInTheDocument();
        });

        const statusSelect = screen.getByDisplayValue('All Status');
        fireEvent.change(statusSelect, { target: { value: 'pending' } });

        await waitFor(() => {
            expect(screen.queryByText('Website Redesign')).not.toBeInTheDocument();
            expect(screen.getByText('Mobile App')).toBeInTheDocument();
        });
    });
});
