import { apiClient } from "./axiosClient";

export interface TeamMemberDTO {
    id: number;
    name: string;
    role: string;
    initials: string;
}

export interface ProjectResponse {
    id: number;
    clientId: number;
    managerId: number;
    name: string;
    description: string;
    type: string;
    startDate: string;
    deadline: string;
    status: string;
    team: TeamMemberDTO[];
}

export interface ProjectUpdateRequest {
    clientId?: number;
    name?: string;
    description?: string;
    type?: string;
    startDate?: string;
    deadline?: string;
    status?: string;
}

export interface ProjectCreateRequest {
    clientId: number;
    name: string;
    description?: string;
    type: string;
    startDate: string;
    deadline: string;
    status?: string;
}

export const ProjectService = {
    getAllProjects: async (params?: Record<string, any>): Promise<ProjectResponse[]> => {
        const response = await apiClient.get('/api/projects', { params });
        return response.data;
    },

    getProjectById: async (id: number): Promise<ProjectResponse> => {
        const response = await apiClient.get(`/api/projects/${id}`);
        return response.data;
    },

    createProject: async (project: ProjectCreateRequest): Promise<ProjectResponse> => {
        const response = await apiClient.post('/api/projects', project);
        return response.data;
    },

    updateProject: async (id: number, project: ProjectUpdateRequest): Promise<ProjectResponse> => {
        const response = await apiClient.put(`/api/projects/${id}`, project);
        return response.data;
    },

    updateProjectTeam: async (id: number, freelancerIds: number[]): Promise<ProjectResponse> => {
        const response = await apiClient.put(`/api/projects/${id}/team`, freelancerIds);
        return response.data;
    },
};
