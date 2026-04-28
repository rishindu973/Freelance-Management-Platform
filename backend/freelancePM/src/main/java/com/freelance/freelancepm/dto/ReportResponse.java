package com.freelance.freelancepm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private BigDecimal totalRevenue;
    private long projectsStarted;
    private long projectsCompleted;
    private long invoicesGenerated;
    private List<DailyRevenue> revenueTimeline;
}
