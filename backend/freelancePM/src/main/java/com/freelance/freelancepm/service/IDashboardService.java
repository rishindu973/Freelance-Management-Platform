package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.DashboardResponse;
import com.freelance.freelancepm.dto.WorkSummaryResponse;

public interface IDashboardService {
    DashboardResponse getDashboard(Integer managerId, int dueSoonDays, int listLimit);
    WorkSummaryResponse getWorkSummary(Integer managerId);
}
