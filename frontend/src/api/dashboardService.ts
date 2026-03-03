import { ProjectResponse } from "./projectService";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

export interface DashboardResponse {
    totalProjects: number;
    activeProjects: number;
    pendingProjects: number;
    completedProjects: number;
    overdueProjects: number;
    dueSoonProjects: number;
    statusBreakdown: Record<string, number>;
    upcomingDeadlines: ProjectResponse[];
    recentCompleted: ProjectResponse[];
    pendingWork: ProjectResponse[];
}

export const DashboardService = {
    getDashboard: async (dueSoonDays: number = 7, limit: number = 5): Promise<DashboardResponse> => {
        const url = `${API_BASE_URL}/api/dashboard?dueSoonDays=${dueSoonDays}&limit=${limit}`;

        const response = await fetch(url, {
            headers: {
                "X-Manager-Id": "1", // Hardcoded for now
            },
        });

        if (!response.ok) {
            throw new Error(`Error fetching dashboard: ${response.statusText}`);
        }

        return response.json();
    }
};
