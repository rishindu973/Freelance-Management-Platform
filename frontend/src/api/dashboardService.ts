import { apiClient } from "./axiosClient";
import { ProjectResponse } from "./projectService";

export interface DashboardResponse {
    totalProjects: number;
    activeProjects: number;
    pendingProjects: number;
    completedProjects: number;
    overdueProjects: number;
    dueSoonProjects: number;
    totalIncome?: number;
    statusBreakdown: Record<string, number>;
    upcomingDeadlines: ProjectResponse[];
    recentCompleted: ProjectResponse[];
    pendingWork: ProjectResponse[];
}

export const DashboardService = {
    getDashboard: async (dueSoonDays: number = 7, limit: number = 5): Promise<DashboardResponse> => {
        const response = await apiClient.get('/api/dashboard', {
            params: { dueSoonDays, limit },
        });
        return response.data;
    }
};
