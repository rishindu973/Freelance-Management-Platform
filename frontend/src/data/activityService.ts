const API_BASE_URL = "http://localhost:8081";

export interface ActivityResponse {
    id: number;
    managerId: number;
    type: "MEMBER_ADDED" | "PROJECT_CREATED" | "INVOICE_SENT";
    description: string;
    timestamp: string; // ISO date string
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
        const query = new URLSearchParams();
        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined && value !== null) {
                    query.append(key, String(value));
                }
            });
        }
        const qs = query.toString();
        const url = `${API_BASE_URL}/api/activities${qs ? `?${qs}` : ""}`;

        const response = await fetch(url, {
            headers: {
                "X-Manager-Id": "1", // Hardcoded for now, same as projectService
            },
        });
        if (!response.ok) {
            throw new Error(`Error fetching activities: ${response.statusText}`);
        }
        return response.json();
    },
};
