import { apiClient } from "./axiosClient";

export interface TaskDTO {
    id: number;
    title: string;
    description: string;
    freelancerEmail: string;
    freelancerName: string;
    status: "todo" | "in-progress" | "done" | string;
    priority: "low" | "medium" | "high" | string;
    deadline: string;
    projectId: number;
}

export interface TaskRequest {
    freelancerId: number;
    title: string;
    description?: string;
    projectId: number;
    deadline: string;
    priority: string;
}

export const TaskService = {
    getTasksByProject: async (projectId: number): Promise<TaskDTO[]> => {
        const response = await apiClient.get(`/api/tasks/project/${projectId}`);
        return response.data;
    },

    createTask: async (request: TaskRequest): Promise<TaskDTO> => {
        const response = await apiClient.post("/api/tasks", request);
        return response.data;
    },

    updateTaskStatus: async (taskId: number, status: string): Promise<TaskDTO> => {
        const response = await apiClient.patch(`/api/tasks/${taskId}/status`, null, {
            params: { status }
        });
        return response.data;
    }
};
