const API_BASE_URL = "http://localhost:8081";

export interface ManagerProfile {
    id: number;
    email: string;
    fullName: string;
    companyName: string;
    contactNumber: string;
}

export const ManagerService = {
    getManagerProfile: async (): Promise<ManagerProfile> => {
        const response = await fetch(`${API_BASE_URL}/api/manager/profile`);
        if (!response.ok) {
            throw new Error(`Error fetching manager profile: ${response.statusText}`);
        }
        return response.json();
    },
};
