package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.dto.DashboardResponse;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.WorkSummaryResponse;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final ProjectRepository projectRepository;
    private final ProfitService profitService;

    @Override
    public DashboardResponse getDashboard(Integer managerId, int dueSoonDays, int listLimit) {
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(Math.max(dueSoonDays, 0));

        long total = projectRepository.countAllByManager(managerId);
        long completed = projectRepository.countCompletedByManager(managerId);
        long pending = projectRepository.countPendingByManager(managerId);
        long active = projectRepository.countActiveByManager(managerId);

        long overdue = projectRepository.countOverdueByManager(managerId, today);
        long dueSoon = projectRepository.countDueSoonByManager(managerId, today, until);

        Map<String, Long> breakdown = toBreakdownMap(projectRepository.statusBreakdown(managerId));

        List<ProjectResponse> upcoming = projectRepository
                .upcomingDeadlines(managerId, today, PageRequest.of(0, listLimit))
                .stream().map(this::toResponse).toList();

        List<ProjectResponse> recentCompleted = projectRepository
                .recentCompleted(managerId, PageRequest.of(0, listLimit))
                .stream().map(this::toResponse).toList();

        List<ProjectResponse> pendingWork = projectRepository.pendingProjects(managerId, PageRequest.of(0, listLimit))
                .stream().map(this::toResponse).toList();

        return DashboardResponse.builder()
                .totalProjects(total)
                .activeProjects(active)
                .pendingProjects(pending)
                .completedProjects(completed)
                .overdueProjects(overdue)
                .dueSoonProjects(dueSoon)
                .totalIncome(profitService.calculateTotalIncome())
                .statusBreakdown(breakdown)
                .upcomingDeadlines(upcoming)
                .recentCompleted(recentCompleted)
                .pendingWork(pendingWork)
                .build();
    }

    @Override
    public WorkSummaryResponse getWorkSummary(Integer managerId) {
        LocalDate today = LocalDate.now();
        LocalDate startThisMonth = today.withDayOfMonth(1);
        LocalDate endThisMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        LocalDate startLastMonth = startThisMonth.minusMonths(1);
        LocalDate endLastMonth = startThisMonth.minusDays(1);

        long completedThisMonth = projectRepository.countCompletedInPeriod(managerId, startThisMonth, endThisMonth);
        long completedLastMonth = projectRepository.countCompletedInPeriod(managerId, startLastMonth, endLastMonth);
        long pendingThisMonth = projectRepository.countPendingInPeriod(managerId, startThisMonth, endThisMonth);
        long pendingLastMonth = projectRepository.countPendingInPeriod(managerId, startLastMonth, endLastMonth);

        double completedGrowth = 0.0;
        if (completedLastMonth > 0) {
            completedGrowth = ((double) (completedThisMonth - completedLastMonth) / completedLastMonth) * 100;
        } else if (completedThisMonth > 0) {
            completedGrowth = 100.0;
        }

        double pendingGrowth = 0.0;
        if (pendingLastMonth > 0) {
            pendingGrowth = ((double) (pendingThisMonth - pendingLastMonth) / pendingLastMonth) * 100;
        } else if (pendingThisMonth > 0) {
            pendingGrowth = 100.0;
        }

        List<ProjectResponse> completedProjects = projectRepository.findCompletedInPeriod(managerId, startThisMonth, endThisMonth)
                .stream().map(this::toResponse).toList();
                
        List<ProjectResponse> pendingProjects = projectRepository.findPendingNearDeadlineInPeriod(managerId, startThisMonth, endThisMonth, PageRequest.of(0, 10))
                .stream().map(this::toResponse).toList();

        return WorkSummaryResponse.builder()
                .completedThisMonth(completedThisMonth)
                .completedLastMonth(completedLastMonth)
                .completedGrowthPercentage(completedGrowth)
                .completedProjectsThisMonth(completedProjects)
                .pendingThisMonth(pendingThisMonth)
                .pendingLastMonth(pendingLastMonth)
                .pendingGrowthPercentage(pendingGrowth)
                .pendingProjectsNearDeadline(pendingProjects)
                .build();
    }

    private Map<String, Long> toBreakdownMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] r : rows) {
            String status = r[0] != null ? String.valueOf(r[0]) : "unknown";
            Long total = ((Number) r[1]).longValue();
            map.put(status, total);
        }
        return map;
    }

    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .clientId(p.getClient() != null ? p.getClient().getId() : null)
                .managerId(p.getManagerId())
                .name(p.getName())
                .description(p.getDescription())
                .type(p.getType())
                .startDate(p.getStartDate())
                .deadline(p.getDeadline())
                .status(p.getStatus())
                .budget(p.getBudget())
                .progressPercentage(p.getProgressPercentage())
                .team(new ArrayList<>())
                .build();
    }
}