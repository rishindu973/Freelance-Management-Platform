package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.dto.*;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectResponse create(Integer managerId, ProjectCreateRequest req) {
        Project p = Project.builder()
                .managerId(managerId)
                .clientId(req.getClientId())
                .name(req.getName())
                .description(req.getDescription())
                .type(req.getType())
                .startDate(req.getStartDate())
                .deadline(req.getDeadline())
                .status("pending") // Default for SCRUM-28
                .build();
        return toResponse(projectRepository.save(p));
    }

    public List<ProjectResponse> list(Integer managerId) {
        // Simple list for SCRUM-29
        return projectRepository.findAllByManagerId(managerId)
                .stream().map(this::toResponse).toList();
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