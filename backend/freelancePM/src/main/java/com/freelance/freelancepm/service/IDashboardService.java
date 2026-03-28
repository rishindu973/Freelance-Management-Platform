package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.DashboardResponse;

public interface IDashboardService {
    DashboardResponse getDashboard(Integer managerId, int dueSoonDays, int listLimit);
}
