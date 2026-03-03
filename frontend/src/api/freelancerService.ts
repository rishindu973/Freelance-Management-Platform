const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

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
    getAllFreelancers: async (): Promise<Freelancer[]> => {
        const response = await fetch(`${API_BASE_URL}/api/freelancer`);
        if (!response.ok) {
            throw new Error(`Error fetching freelancers: ${response.statusText}`);
        }
        return response.json();
    },

    getFreelancerById: async (id: number): Promise<Freelancer> => {
        const response = await fetch(`${API_BASE_URL}/api/freelancer/${id}`);
        if (!response.ok) {
            throw new Error(`Error fetching freelancer: ${response.statusText}`);
        }
        return response.json();
    },

    createFreelancer: async (freelancer: Freelancer): Promise<Freelancer> => {
        const response = await fetch(`${API_BASE_URL}/api/freelancer/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(freelancer),
        });
        if (!response.ok) {
            throw new Error(`Error creating freelancer: ${response.statusText}`);
        }
        return response.json();
    },

    updateFreelancer: async (id: number, freelancer: Freelancer): Promise<Freelancer> => {
        const response = await fetch(`${API_BASE_URL}/api/freelancer/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(freelancer),
        });
        if (!response.ok) {
            throw new Error(`Error updating freelancer: ${response.statusText}`);
        }
        return response.json();
    },

    deleteFreelancer: async (id: number): Promise<void> => {
        const response = await fetch(`${API_BASE_URL}/api/freelancer/${id}`, {
            method: "DELETE",
        });
        if (!response.ok) {
            throw new Error(`Error deleting freelancer: ${response.statusText}`);
        }
    },
};
