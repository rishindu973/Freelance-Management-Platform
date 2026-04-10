import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi, type Mock } from 'vitest';
import FreelancerProjectDetail from '../pages/freelancer/FreelancerProjectDetail';
import { FreelancerPortalService } from '@/api/freelancerPortalService';

vi.mock('@/api/freelancerPortalService', () => ({
    FreelancerPortalService: {
        getAssignments: vi.fn(),
        getProfile: vi.fn(),
        updateAvailability: vi.fn(),
    },
}));

vi.mock('sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn(),
    },
}));

const mockProjects = [
    {
        id: 42,
        name: 'E-commerce Platform',
        status: 'in progress',
        deadline: '2026-07-15',
        startDate: '2026-02-01',
        clientName: 'ShopCo Ltd',
        description: 'Build an e-commerce platform',
        type: 'fullstack',
        clientId: 5,
        managerId: 1,
        team: [
            { id: 1, name: 'John Doe', role: 'Developer', initials: 'JD' },
            { id: 2, name: 'Jane Smith', role: 'Designer', initials: 'JS' },
        ],
    },
];

describe('FreelancerProjectDetail', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const renderWithRoute = (projectId: string) => {
        return render(
            <MemoryRouter initialEntries={[`/freelancer/projects/${projectId}`]}>
                <Routes>
                    <Route path="/freelancer/projects/:id" element={<FreelancerProjectDetail />} />
                </Routes>
            </MemoryRouter>
        );
    };

    it('shows loading state initially', () => {
        (FreelancerPortalService.getAssignments as Mock).mockReturnValue(new Promise(() => { }));
        renderWithRoute('42');
        expect(screen.getByText(/Loading project details/i)).toBeInTheDocument();
    });

    it('shows access denied when project not found in assignments', async () => {
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue([]);
        renderWithRoute('999');

        await waitFor(() => {
            expect(screen.getByText(/Project not found or access denied/i)).toBeInTheDocument();
        });
    });

    it('renders project details with client name', async () => {
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRoute('42');

        await waitFor(() => {
            expect(screen.getByText('E-commerce Platform')).toBeInTheDocument();
            expect(screen.getByText('ShopCo Ltd')).toBeInTheDocument();
            expect(screen.getByText(/Build an e-commerce platform/)).toBeInTheDocument();
        });
    });

    it('displays team members', async () => {
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRoute('42');

        await waitFor(() => {
            expect(screen.getByText(/John Doe/)).toBeInTheDocument();
            expect(screen.getByText(/Jane Smith/)).toBeInTheDocument();
        });
    });

    it('displays deadline information', async () => {
        (FreelancerPortalService.getAssignments as Mock).mockResolvedValue(mockProjects);
        renderWithRoute('42');

        await waitFor(() => {
            expect(screen.getByText('Deadline')).toBeInTheDocument();
        });
    });
});
