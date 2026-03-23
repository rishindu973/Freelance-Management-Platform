package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.entity.Activity;
import com.freelance.freelancepm.dto.ProjectCreateRequest;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.ProjectUpdateRequest;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ActivityService activityService;

    public ProjectResponse create(Long managerId, ProjectCreateRequest req) {
        Project p = Project.builder()
                .managerId(managerId)
                .clientId(req.getClientId())
                .name(req.getName())
                .description(req.getDescription())
                .type(req.getType())
                .startDate(req.getStartDate())
                .deadline(req.getDeadline())
                .status(req.getStatus() != null ? req.getStatus() : "pending")
                .build();

        Project savedProject = projectRepository.save(p);
        
        activityService.logActivity(
                managerId, 
                Activity.ActivityType.PROJECT_CREATED, 
                "Project '" + savedProject.getName() + "' was created."
        );

        return toResponse(savedProject);
    }

    public List<ProjectResponse> list(Long managerId, String status, Long clientId, String search, LocalDate from, LocalDate to, Boolean isCritical) {
        Specification<Project> spec = Specification.where(ProjectSpecifications.managerIdEquals(managerId));

        if (status != null && !status.isBlank()) {
            spec = spec.and(ProjectSpecifications.statusEquals(status));
        }
        if (clientId != null) {
            spec = spec.and(ProjectSpecifications.clientIdEquals(clientId));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(ProjectSpecifications.nameOrDescriptionContains(search));
        }
        if (from != null && to != null) {
            spec = spec.and(ProjectSpecifications.deadlineBetween(from, to));
        }
        if (Boolean.TRUE.equals(isCritical)) {
            LocalDate inSevenDays = LocalDate.now().plusDays(7);
            spec = spec.and(ProjectSpecifications.deadlineBeforeOrEquals(inSevenDays));
            spec = spec.and((root, query, cb) -> cb.notEqual(cb.lower(root.get("status")), "completed"));
        }

        return projectRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(this::toResponse).toList();
    }

    public ProjectResponse get(Long managerId, Long projectId) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        return toResponse(p);
    }

    public ProjectResponse update(Long managerId, Long projectId, ProjectUpdateRequest req) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (req.getClientId() != null) p.setClientId(req.getClientId());
        if (req.getName() != null) p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getType() != null) p.setType(req.getType());
        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getDeadline() != null) p.setDeadline(req.getDeadline());
        if (req.getStatus() != null) p.setStatus(req.getStatus());

        return toResponse(projectRepository.save(p));
    }

    public void delete(Long managerId, Long projectId) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        projectRepository.delete(p);
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