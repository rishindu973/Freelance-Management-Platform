package com.freelance.freelancepm.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardResponse {
    private long totalProjects;
    private long activeProjects;
    private long pendingProjects;
    private long completedProjects;

    private long overdueProjects;
    private long dueSoonProjects;
    private java.math.BigDecimal totalIncome;

    // for charts (pie/bar)
    private Map<String, Long> statusBreakdown;

    // for dashboard sections
    private List<ProjectResponse> upcomingDeadlines;
    private List<ProjectResponse> recentCompleted;
    private List<ProjectResponse> pendingWork;
}