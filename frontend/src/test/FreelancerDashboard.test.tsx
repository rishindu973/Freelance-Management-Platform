import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi, type Mock } from 'vitest';
import FreelancerDashboard from '../pages/freelancer/FreelancerDashboard';
import { FreelancerPortalService } from '@/api/freelancerPortalService';

vi.mock('@/api/freelancerPortalService', () => ({
    FreelancerPortalService: {
        getAssignments: vi.fn(),
        getProfile: vi.fn(),
        updateAvailability: vi.fn(),
    },
}));

vi.mock('@/components/ui/use-toast', () => ({
    useToast: () => ({ toast: vi.fn() }),
}));

const mockProfile = {
    id: 1,
    fullName: 'Test User',
    title: 'Developer',
    contactNumber: '1234567890',
    salary: 50,
    status: 'available',
    driveLink: 'http://test.com',
};

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

describe('FreelancerDashboard', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const renderWithRouter = (ui: React.ReactElement) => {
        return render(<BrowserRouter>{ui}</BrowserRouter>);
    };

    it('shows loading state initially', () => {
        (FreelancerPortalService.getProfile as Mock).mockReturnValue(new Promise(() => { }));
        (FreelancerPortalService.getAssignments as Mock).mockReturnValue(new Promise(() => { }));
        renderWithRouter(<FreelancerDashboard />);
        expect(screen.getByText(/Loading dashboard/i)).toBeInTheDocument();
    });

    it('renders empty state when no projects', async () => {
        (FreelancerPortalService.getProfile as Mock).mockResolvedValue(mockProfile);
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue([]);
        renderWithRouter(<FreelancerDashboard />);

        await waitFor(() => {
            expect(screen.getByText(/No projects assigned yet/i)).toBeInTheDocument();
        });
    });

    it('renders My Tasks widget with correct counts', async () => {
        (FreelancerPortalService.getProfile as Mock).mockResolvedValue(mockProfile);
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRouter(<FreelancerDashboard />);

        await waitFor(() => {
            expect(screen.getByText('My Tasks')).toBeInTheDocument();
            expect(screen.getByText('2')).toBeInTheDocument(); // total
            const pendingElements = screen.getAllByText('1');
            expect(pendingElements.length).toBeGreaterThan(0); // 1 pending, 1 in progress
            expect(screen.getByText('0')).toBeInTheDocument(); // 0 completed
        });
    });

    it('renders project list with client names', async () => {
        (FreelancerPortalService.getProfile as Mock).mockResolvedValue(mockProfile);
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRouter(<FreelancerDashboard />);

        await waitFor(() => {
            expect(screen.getByText('Website Redesign')).toBeInTheDocument();
            expect(screen.getByText('Mobile App')).toBeInTheDocument();
            expect(screen.getByText(/Acme Corp/)).toBeInTheDocument();
            expect(screen.getByText(/Tech Inc/)).toBeInTheDocument();
        });
    });

    it('handles availability toggle correctly', async () => {
        (FreelancerPortalService.getProfile as Mock).mockResolvedValue({ ...mockProfile, status: 'unavailable' });
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        (FreelancerPortalService.updateAvailability as Mock).mockResolvedValue({ message: "Updated", status: "available" });

        renderWithRouter(<FreelancerDashboard />);

        await waitFor(() => {
            expect(screen.getByText('Unavailable')).toBeInTheDocument();
        });

        const toggle = screen.getByRole('switch');
        fireEvent.click(toggle);

        await waitFor(() => {
            expect(FreelancerPortalService.updateAvailability).toHaveBeenCalledWith('available');
            expect(screen.getByText('Available')).toBeInTheDocument();
        });
    });

    it('has Upload Work action buttons', async () => {
        (FreelancerPortalService.getProfile as Mock).mockResolvedValue(mockProfile);
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRouter(<FreelancerDashboard />);

        await waitFor(() => {
            const uploadButtons = screen.getAllByText('Upload Work');
            expect(uploadButtons.length).toBe(2);
        });
    });
});
