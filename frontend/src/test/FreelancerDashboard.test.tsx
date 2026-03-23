import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import FreelancerDashboard from "@/pages/freelancer/FreelancerDashboard";

// Mock the FreelancerPortalService
vi.mock("@/api/freelancerPortalService", () => ({
  FreelancerPortalService: {
    getAssignments: vi.fn(),
    getProfile: vi.fn(),
  },
}));

import { FreelancerPortalService } from "@/api/freelancerPortalService";

// Deadlines are set far in the future so they don't trigger the "due soon" section
const mockAssignments = [
  {
    id: 1,
    clientId: 10,
    managerId: 1,
    name: "Website Redesign",
    description: "Redesign the company website",
    type: "Web Development",
    startDate: "2026-03-01",
    deadline: "2026-12-15",
    status: "in_progress",
    team: [
      { id: 1, name: "John Doe", role: "Developer", initials: "JD" },
    ],
  },
  {
    id: 2,
    clientId: 11,
    managerId: 1,
    name: "Logo Design",
    description: "Create new brand logo",
    type: "Design",
    startDate: "2026-03-10",
    deadline: "2026-11-25",
    status: "completed",
    team: [],
  },
  {
    id: 3,
    clientId: 12,
    managerId: 1,
    name: "Urgent Task",
    description: "Fix critical bug",
    type: "Bug Fix",
    startDate: "2026-03-20",
    deadline: "2026-12-30",
    status: "pending",
    team: [],
  },
];

const renderDashboard = () =>
  render(
    <BrowserRouter>
      <FreelancerDashboard />
    </BrowserRouter>
  );

describe("FreelancerDashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // NOTE: jsdom renders both desktop (hidden md:block) table AND mobile cards
  // since CSS media queries aren't applied. All text assertions use getAllByText.

  it("renders assigned projects when data is available", async () => {
    vi.mocked(FreelancerPortalService.getAssignments).mockResolvedValue(
      mockAssignments
    );

    renderDashboard();

    await waitFor(() => {
      // Use getAllByText because both desktop table and mobile cards render the name
      const elements = screen.getAllByText("Website Redesign");
      expect(elements.length).toBeGreaterThan(0);
    });

    expect(screen.getAllByText("Logo Design").length).toBeGreaterThan(0);
    expect(screen.getAllByText("Urgent Task").length).toBeGreaterThan(0);
  });

  it("renders empty state when no assignments exist", async () => {
    vi.mocked(FreelancerPortalService.getAssignments).mockResolvedValue([]);

    renderDashboard();

    await waitFor(() => {
      expect(
        screen.getByText("No projects assigned yet")
      ).toBeInTheDocument();
    });

    expect(
      screen.getByText(/your manager hasn't assigned any projects/i)
    ).toBeInTheDocument();
  });

  it("renders color-coded status badges", async () => {
    vi.mocked(FreelancerPortalService.getAssignments).mockResolvedValue(
      mockAssignments
    );

    renderDashboard();

    await waitFor(() => {
      const elements = screen.getAllByText("Website Redesign");
      expect(elements.length).toBeGreaterThan(0);
    });

    // "In Progress" appears as KPI label + status badges (desktop + mobile).
    const inProgressElements = screen.getAllByText("In Progress");
    const hasBlueBadge = inProgressElements.some((el) =>
      el.className.includes("text-blue-700")
    );
    expect(hasBlueBadge).toBe(true);

    // "Completed" appears as KPI label + status badges.
    const completedElements = screen.getAllByText("Completed");
    const hasGreenBadge = completedElements.some((el) =>
      el.className.includes("text-green-700")
    );
    expect(hasGreenBadge).toBe(true);
  });

  it("renders KPI cards with correct counts", async () => {
    vi.mocked(FreelancerPortalService.getAssignments).mockResolvedValue(
      mockAssignments
    );

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText("My Tasks")).toBeInTheDocument();
    });

    // Total projects = 3
    expect(screen.getByText("3")).toBeInTheDocument();

    // KPI subtitles confirm cards rendered
    expect(screen.getByText("Total assigned")).toBeInTheDocument();
    expect(screen.getByText("Currently active")).toBeInTheDocument();
    expect(screen.getByText("Finished")).toBeInTheDocument();
    expect(screen.getByText("Within 7 days")).toBeInTheDocument();
  });

  it("shows loading state initially", () => {
    vi.mocked(FreelancerPortalService.getAssignments).mockReturnValue(
      new Promise(() => {}) // Never resolves - stays loading
    );

    renderDashboard();

    expect(
      screen.getByText("Loading your assignments...")
    ).toBeInTheDocument();
  });

  it("shows error state when API fails", async () => {
    vi.mocked(FreelancerPortalService.getAssignments).mockRejectedValue(
      new Error("Network error")
    );

    renderDashboard();

    await waitFor(() => {
      expect(
        screen.getByText("Failed to load assignments. Please try again later.")
      ).toBeInTheDocument();
    });
  });
});
