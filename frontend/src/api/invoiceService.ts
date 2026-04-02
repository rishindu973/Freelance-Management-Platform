import { apiClient } from "./axiosClient";

export interface InvoiceLineItem {
  id?: number;
  description: string;
  quantity: number;
  unitPrice: number;
}

export interface Invoice {
  id?: number;
  clientId: number;
  projectId?: number;
  status: 'DRAFT' | 'FINAL';
  lineItems: InvoiceLineItem[];
  subtotal?: number;
  tax?: number;
  totalAmount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export const InvoiceService = {
  getAllInvoices: async (): Promise<Invoice[]> => {
    const response = await apiClient.get('/api/invoices');
    return response.data;
  },

  getInvoiceById: async (id: number): Promise<Invoice> => {
    const response = await apiClient.get(`/api/invoices/${id}`);
    return response.data;
  },

  createInvoice: async (invoice: Partial<Invoice>): Promise<Invoice> => {
    const response = await apiClient.post('/api/invoices', invoice);
    return response.data;
  },

  updateInvoice: async (id: number, invoice: Partial<Invoice>): Promise<Invoice> => {
    const response = await apiClient.put(`/api/invoices/${id}`, invoice);
    return response.data;
  },
};
