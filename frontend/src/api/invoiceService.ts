import apiClient from './axiosClient';

export interface InvoiceCreateRequest {
    clientId: number;
    projectId?: number;
    status?: string;
    subtotal?: number;
    tax?: number;
    total: number;
    type?: string;
    description?: string;
    dueDate?: string;
    year: number;
}

export interface InvoiceLineItemRequest {
    description: string;
    quantity: number;
    unitPrice: number;
}

export interface InvoiceResponse {
    id: number;
    clientId: number;
    projectId: number;
    status: string;
    subtotal: number;
    tax: number;
    total: number;
    dueDate: string;
    createdAt: string;
    updatedAt: string;
    version: number;
    remainingBalance?: number;
    type: string;
    description: string;
    invoiceNumber: string;
    sequenceNumber: number;
    year: number;
    lineItems: any[];
}

export const InvoiceService = {
    getAllInvoices: async (): Promise<InvoiceResponse[]> => {
        const response = await apiClient.get('/api/invoices');
        return response.data;
    },
    getInvoiceById: async (id: number): Promise<InvoiceResponse> => {
        const response = await apiClient.get(`/api/invoices/${id}`);
        return response.data;
    },
    createInvoice: async (data: InvoiceCreateRequest): Promise<InvoiceResponse> => {
        const response = await apiClient.post('/api/invoices', data);
        return response.data;
    },
    updateInvoiceStatus: async (id: number, status: string): Promise<InvoiceResponse> => {
        const response = await apiClient.patch(`/api/invoices/${id}/status`, { status });
        return response.data;
    },
};
