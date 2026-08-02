import { apiClient } from "./axiosClient";

export interface ActivityResponse {
    id: number;
    managerId: number;
    type: "MEMBER_ADDED" | "PROJECT_CREATED" | "INVOICE_SENT";
    description: string;
    timestamp: string;
}

export interface ActivityPageResponse {
    content: ActivityResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const ActivityService = {
    getActivities: async (params?: Record<string, any>): Promise<ActivityPageResponse> => {
        const response = await apiClient.get("/api/activities", { params });
        return response.data;
    },
};
