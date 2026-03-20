import { apiClient } from "./axiosClient";

export interface Client {
  id?: number;
  name: string;
  email: string;
  phone: string;
}

export const ClientService = {
  getAllClients: async (): Promise<Client[]> => {
    const response = await apiClient.get('/clients/all');
    return response.data;
  },

  addClient: async (client: Client): Promise<Client> => {
    const response = await apiClient.post('/clients/add', client);
    return response.data;
  },

  deleteClient: async (id: number): Promise<string> => {
    const response = await apiClient.delete(`/clients/delete/${id}`);
    return response.data;
  },
};
