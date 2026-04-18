import apiClient from './axiosClient';

export interface PageResponse<T> {
  content: T[];
  pageable: {
    sort: { empty: boolean; sorted: boolean; unsorted: boolean };
    offset: number;
    pageNumber: number;
    pageSize: number;
    unpaged: boolean;
    paged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: { empty: boolean; sorted: boolean; unsorted: boolean };
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}

export interface InvoiceListDTO {
  id: number;
  invoiceNumber: string;
  clientId: number;
  clientName: string;
  total: number;
  createdAt: string;
  status: string;
  displayStatus: string;
}

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
  lineItems?: InvoiceLineItemRequest[];
}

export interface InvoiceLineItemRequest {
  description: string;
  quantity: number;
  unitPrice: number;
  amount: number;
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
  failureReason?: string;
  lastSentAt?: string;
  lineItems: any[];
}

export const InvoiceService = {
  getAllInvoices: async (params?: {
    clientId?: number;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    direction?: string;
  }): Promise<PageResponse<InvoiceListDTO>> => {
    const response = await apiClient.get('/api/invoices', { params });
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
  updateInvoiceStatus: async (id: number, targetStatus: string, amount?: number): Promise<Invoice> => {
    const response = await apiClient.patch(`/api/invoices/${id}/status`, { targetStatus, amount });
    return response.data;
  },
  addPayment: async (id: number, data: { amount: number; paymentMethod?: string; paymentDate?: string; referenceNumber?: string }): Promise<void> => {
    await apiClient.post(`/api/invoices/${id}/payments`, data);
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
  sendInvoice: async (id: number, recipients: string[]): Promise<void> => {
    await apiClient.post(`/api/invoices/${id}/send`, { recipients });
  },
  fetchInvoicePdf: async (invoiceId: number): Promise<Blob> => {
    try {
      const response = await apiClient.get(`/api/invoices/${invoiceId}/pdf`, {
        responseType: 'blob',
      });
      return response.data;
    } catch (error) {
      console.error('Failed to fetch invoice PDF from server:', error);
      throw error;
    }
  },
  updateInvoice: async (id: number, data: any): Promise<Invoice> => {
    const response = await apiClient.put(`/api/invoices/${id}`, data);
    return response.data;
  },
};
