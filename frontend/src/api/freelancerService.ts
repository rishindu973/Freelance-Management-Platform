import { apiClient } from "./axiosClient";
import { PageResponse } from "./types";

export interface Freelancer {
    id?: number;
    email: string;
    password?: string;
    role?: string;
    fullName: string;
    title: string;
    contactNumber: string;
    salary: number;
    status: string;
    driveLink?: string;
}

export const FreelancerService = {
    getAllFreelancers: async (page = 0, size = 10): Promise<PageResponse<Freelancer>> => {
        const response = await apiClient.get('/api/freelancers', { params: { page, size } });
        return response.data;
    },

    getFreelancerById: async (id: number): Promise<Freelancer> => {
        const response = await apiClient.get(`/api/freelancers/${id}`);
        return response.data;
    },

    createFreelancer: async (freelancer: Freelancer): Promise<Freelancer> => {
        const response = await apiClient.post('/api/freelancers/create', freelancer);
        return response.data;
    },

    updateFreelancer: async (id: number, freelancer: Freelancer): Promise<Freelancer> => {
        const response = await apiClient.put(`/api/freelancers/${id}`, freelancer);
        return response.data;
    },

    deleteFreelancer: async (id: number): Promise<void> => {
        await apiClient.delete(`/api/freelancers/${id}`);
    },
};
