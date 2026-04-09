import apiClient from './axiosClient';

export interface PaymentCreateRequest {
    invoiceId: number;
    amount: number;
    status?: string;
}

export interface PaymentResponse {
    id: number;
    invoiceId: number;
    amount: number;
    paymentDate: string;
    status: string;
}

export const PaymentService = {
    getAllPayments: async (): Promise<PaymentResponse[]> => {
        const response = await apiClient.get('/api/payments');
        return response.data;
    },
    getPaymentById: async (id: number): Promise<PaymentResponse> => {
        const response = await apiClient.get(`/api/payments/${id}`);
        return response.data;
    },
    createPayment: async (data: PaymentCreateRequest): Promise<PaymentResponse> => {
        const response = await apiClient.post('/api/payments', data);
        return response.data;
    },
    updatePaymentStatus: async (id: number, status: string): Promise<PaymentResponse> => {
        const response = await apiClient.patch(`/api/payments/${id}/status`, { status });
        return response.data;
    },
};
