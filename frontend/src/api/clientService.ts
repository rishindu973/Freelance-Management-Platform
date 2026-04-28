import { apiClient } from "./axiosClient";

export interface Client {
  id?: number;
  name: string;
  email: string;
  phone: string;
  address?: string;
}

export const ClientService = {
  getAllClients: async (): Promise<Client[]> => {
    const response = await apiClient.get('/api/clients');
    return response.data;
  },

  getClientById: async (id: number): Promise<Client> => {
    const response = await apiClient.get(`/api/clients/${id}`);
    return response.data;
  },

  addClient: async (client: Client): Promise<Client> => {
    const response = await apiClient.post('/api/clients', client);
    return response.data;
  },

  updateClient: async (id: number, client: Client): Promise<Client> => {
    const response = await apiClient.put(`/api/clients/${id}`, client);
    return response.data;
  },

  deleteClient: async (id: number): Promise<string> => {
    const response = await apiClient.delete(`/api/clients/${id}`);
    return response.data;
  },
};
