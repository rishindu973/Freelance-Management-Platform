import apiClient from './axiosClient';

export interface DailyRevenue {
    date: string;
    amount: number;
}

export interface ReportResponse {
    totalRevenue: number;
    projectsStarted: number;
    projectsCompleted: number;
    invoicesGenerated: number;
    revenueTimeline: DailyRevenue[];
}

export const ReportService = {
    getReport: async (startDate?: string, endDate?: string): Promise<ReportResponse> => {
        const params: any = {};
        if (startDate) params.startDate = startDate;
        if (endDate) params.endDate = endDate;

        const response = await apiClient.get('/api/reports', { params });
        return response.data;
    }
};
