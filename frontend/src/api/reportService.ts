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
    },
    downloadReportPdf: async (startDate: string, endDate: string, onProgress?: (progress: number) => void): Promise<{ blob: Blob, filename: string }> => {
        const params: any = {};
        if (startDate) params.startDate = startDate;
        if (endDate) params.endDate = endDate;

        const response = await apiClient.get('/api/reports/download', {
            params,
            responseType: 'blob',
            onDownloadProgress: (progressEvent) => {
                if (progressEvent.total && onProgress) {
                    const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                    onProgress(percentCompleted);
                }
            }
        });

        const disposition = response.headers['content-disposition'];
        let filename = 'report.pdf';
        if (disposition && disposition.indexOf('attachment') !== -1) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = filenameRegex.exec(disposition);
            if (matches != null && matches[1]) {
                filename = matches[1].replace(/['"]/g, '');
            }
        }
        return { blob: response.data, filename };
    }
};
