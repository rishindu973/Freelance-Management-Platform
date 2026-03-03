const API_BASE_URL = "http://localhost:8081";

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
        const query = new URLSearchParams();
        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined && value !== null) {
                    query.append(key, String(value));
                }
            });
        }
        const qs = query.toString();
        const url = `${API_BASE_URL}/api/projects${qs ? `?${qs}` : ""}`;

        const response = await fetch(url, {
            headers: {
                "X-Manager-Id": "1", // Hardcoded for now
            },
        });
        if (!response.ok) {
            throw new Error(`Error fetching projects: ${response.statusText}`);
        }
        return response.json();
    },

    getProjectById: async (id: number): Promise<ProjectResponse> => {
        const response = await fetch(`${API_BASE_URL}/api/projects/${id}`, {
            headers: {
                "X-Manager-Id": "1",
            },
        });
        if (!response.ok) {
            throw new Error(`Error fetching project: ${response.statusText}`);
        }
        return response.json();
    },

    createProject: async (project: ProjectCreateRequest): Promise<ProjectResponse> => {
        const response = await fetch(`${API_BASE_URL}/api/projects`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Manager-Id": "1",
            },
            body: JSON.stringify(project),
        });
        if (!response.ok) {
            throw new Error(`Error creating project: ${response.statusText}`);
        }
        return response.json();
    },

    updateProject: async (id: number, project: ProjectUpdateRequest): Promise<ProjectResponse> => {
        const response = await fetch(`${API_BASE_URL}/api/projects/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "X-Manager-Id": "1",
            },
            body: JSON.stringify(project),
        });
        if (!response.ok) {
            throw new Error(`Error updating project: ${response.statusText}`);
        }
        return response.json();
    },

    updateProjectTeam: async (id: number, freelancerIds: number[]): Promise<ProjectResponse> => {
        const response = await fetch(`${API_BASE_URL}/api/projects/${id}/team`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "X-Manager-Id": "1",
            },
            body: JSON.stringify(freelancerIds),
        });
        if (!response.ok) {
            throw new Error(`Error updating project team: ${response.statusText}`);
        }
        return response.json();
    },
};
