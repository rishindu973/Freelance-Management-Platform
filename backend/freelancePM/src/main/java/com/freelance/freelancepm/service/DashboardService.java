package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.dto.DashboardResponse;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;

    public DashboardResponse getDashboard(Long managerId, int dueSoonDays, int listLimit) {
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(Math.max(dueSoonDays, 0));

        long total = projectRepository.countAllByManager(managerId);
        long completed = projectRepository.countCompletedByManager(managerId);
        long pending = projectRepository.countPendingByManager(managerId);
        long active = projectRepository.countActiveByManager(managerId);

        long overdue = projectRepository.countOverdueByManager(managerId, today);
        long dueSoon = projectRepository.countDueSoonByManager(managerId, today, until);

        Map<String, Long> breakdown = toBreakdownMap(projectRepository.statusBreakdown(managerId));

        List<ProjectResponse> upcoming = projectRepository.upcomingDeadlines(managerId, today, listLimit)
                .stream().map(this::toResponse).toList();

        List<ProjectResponse> recentCompleted = projectRepository.recentCompleted(managerId, listLimit)
                .stream().map(this::toResponse).toList();

        List<ProjectResponse> pendingWork = projectRepository.pendingProjects(managerId, listLimit)
                .stream().map(this::toResponse).toList();

        return DashboardResponse.builder()
                .totalProjects(total)
                .activeProjects(active)
                .pendingProjects(pending)
                .completedProjects(completed)
                .overdueProjects(overdue)
                .dueSoonProjects(dueSoon)
                .statusBreakdown(breakdown)
                .upcomingDeadlines(upcoming)
                .recentCompleted(recentCompleted)
                .pendingWork(pendingWork)
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
                .clientId(p.getClientId())
                .managerId(p.getManagerId())
                .name(p.getName())
                .description(p.getDescription())
                .type(p.getType())
                .startDate(p.getStartDate())
                .deadline(p.getDeadline())
                .status(p.getStatus())
                .build();
    }
}