import { apiClient } from "./axiosClient";
import { ProjectResponse } from "./projectService";

export interface FreelancerProfile {
    id: number;
    fullName: string;
    title: string;
    contactNumber: string;
    salary: number;
    status: string;
    driveLink: string;
}

export const FreelancerPortalService = {
    getAssignments: async (): Promise<ProjectResponse[]> => {
        const response = await apiClient.get("/api/freelancer/assignments");
        return response.data;
    },

    getProfile: async (): Promise<FreelancerProfile> => {
        const response = await apiClient.get("/api/freelancer/profile");
        return response.data;
    },

    updateAvailability: async (status: string): Promise<{ message: string; status: string }> => {
        const response = await apiClient.patch('/api/freelancer/availability', { status });
        return response.data;
    },
};
