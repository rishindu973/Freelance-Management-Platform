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

export interface Invoice {
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
  type: string;
  description: string;
  invoiceNumber: string;
  sequenceNumber: number;
  year: number;
  lineItems: any[];
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
  createInvoice: async (data: InvoiceCreateRequest): Promise<Invoice> => {
    const response = await apiClient.post('/api/invoices', data);
    return response.data;
  },
  updateInvoiceStatus: async (id: number, status: string): Promise<Invoice> => {
    const response = await apiClient.patch(`/api/invoices/${id}/status`, { status });
    return response.data;
  },
  downloadInvoicePdf: async (
    id: number,
    onProgress?: (progress: number) => void
  ): Promise<{ blob: Blob; filename: string | null }> => {
    const response = await apiClient.get(`/api/invoices/${id}/pdf`, {
      responseType: 'blob',
      onDownloadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percentCompleted);
        }
      },
    });

    let filename: string | null = null;
    const contentDisposition = response.headers['content-disposition'];
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1].replace(/['"]/g, '');
      }
    }

    return { blob: response.data, filename };
  },
};
