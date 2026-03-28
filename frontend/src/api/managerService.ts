import { apiClient } from "./axiosClient";

export interface ManagerProfile {
    id: number;
    email: string;
    fullName: string;
    companyName: string;
    contactNumber: string;
}

export const ManagerService = {
    getManagerProfile: async (): Promise<ManagerProfile> => {
        const response = await apiClient.get('/api/manager/profile');
        return response.data;
    },
};
