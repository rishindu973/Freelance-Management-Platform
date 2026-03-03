const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

export interface Client {
  id?: number;
  name: string;
  email: string;
  phone: string;
}

export const ClientService = {
  getAllClients: async (): Promise<Client[]> => {
    const response = await fetch(`${API_BASE_URL}/clients/all`);
    if (!response.ok) {
      throw new Error(`Error fetching clients: ${response.statusText}`);
    }
    return response.json();
  },

  addClient: async (client: Client): Promise<Client> => {
    const response = await fetch(`${API_BASE_URL}/clients/add`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(client),
    });
    if (!response.ok) {
      throw new Error(`Error adding client: ${response.statusText}`);
    }
    return response.json();
  },

  deleteClient: async (id: number): Promise<string> => {
    const response = await fetch(`${API_BASE_URL}/clients/delete/${id}`, {
      method: "DELETE",
    });
    if (!response.ok) {
      throw new Error(`Error deleting client: ${response.statusText}`);
    }
    return response.text();
  },
};
