import { apiClient } from "./axiosClient";
import { ProjectResponse } from "./projectService";

export interface WorkSummaryResponse {
    completedThisMonth: number;
    completedLastMonth: number;
    completedGrowthPercentage: number;
    completedProjectsThisMonth: ProjectResponse[];

    pendingThisMonth: number;
    pendingLastMonth: number;
    pendingGrowthPercentage: number;
    pendingProjectsNearDeadline: ProjectResponse[];
}

export const WorkSummaryService = {
    getWorkSummary: async (): Promise<WorkSummaryResponse> => {
        const response = await apiClient.get('/api/dashboard/work-summary');
        return response.data;
    }
};
