package com.freelance.freelancepm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSummaryResponse {
    
    // Completed
    private long completedThisMonth;
    private long completedLastMonth;
    private double completedGrowthPercentage;
    private List<ProjectResponse> completedProjectsThisMonth;

    // Pending
    private long pendingThisMonth;
    private long pendingLastMonth;
    private double pendingGrowthPercentage;
    private List<ProjectResponse> pendingProjectsNearDeadline;

}
