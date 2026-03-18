package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.dto.ProjectCreateRequest;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.ProjectUpdateRequest;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.repository.ProjectRepository;
import com.freelance.freelancepm.repository.FreelancerRepository;
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
    private final FreelancerRepository freelancerRepository;

    // ----------------- Manager Methods (unchanged) -----------------
    public ProjectResponse create(Integer managerId, ProjectCreateRequest req) {
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

        return toResponse(p, p.getClientId());
    }

    public List<ProjectResponse> list(Integer managerId, String status, Integer clientId, String search, LocalDate from,
                                      LocalDate to, Boolean isCritical) {
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
            spec = spec.and(ProjectSpecifications.isCritical(LocalDate.now()));
        }

        return projectRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(p -> toResponse(p, p.getClientId()))
                .toList();
    }

    public ProjectResponse get(Integer managerId, Integer projectId) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        return toResponse(p, p.getClientId());
    }

    public ProjectResponse update(Integer managerId, Integer projectId, ProjectUpdateRequest req) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (req.getClientId() != null)
            p.setClientId(req.getClientId());
        if (req.getName() != null)
            p.setName(req.getName());
        if (req.getDescription() != null)
            p.setDescription(req.getDescription());
        if (req.getType() != null)
            p.setType(req.getType());
        if (req.getStartDate() != null)
            p.setStartDate(req.getStartDate());
        if (req.getDeadline() != null)
            p.setDeadline(req.getDeadline());
        if (req.getStatus() != null)
            p.setStatus(req.getStatus());

        return toResponse(p, p.getClientId());
    }

    @org.springframework.transaction.annotation.Transactional
    public ProjectResponse updateTeam(Integer managerId, Integer projectId, List<Integer> freelancerIds) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        List<com.freelance.freelancepm.entity.Freelancer> freelancers = freelancerRepository.findAllById(freelancerIds);
        p.setTeam(freelancers);

        return toResponse(p, p.getClientId());
    }

    public void delete(Integer managerId, Integer projectId) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        projectRepository.delete(p);
    }

    // ----------------- Client Methods -----------------
    public List<ProjectResponse> getProjectsByClient(Integer clientId, String status, LocalDate from, LocalDate to) {
        List<Project> projects;

        if (status != null || from != null || to != null) {
            projects = projectRepository.filterProjects(clientId, status, from, to);
        } else {
            projects = projectRepository.findByClientId(clientId);
        }

        return projects.stream()
                .map(p -> toResponse(p, p.getClient() != null ? p.getClient().getId() : clientId))
                .toList();
    }

    // ----------------- Mapper -----------------
    private ProjectResponse toResponse(Project p, Integer clientId) {
        List<com.freelance.freelancepm.dto.TeamMemberDTO> teamDto = p.getTeam() != null
                ? p.getTeam().stream()
                .map(f -> com.freelance.freelancepm.dto.TeamMemberDTO.builder()
                        .id(f.getId())
                        .name(f.getFullName())
                        .role(f.getTitle())
                        .initials(getInitials(f.getFullName()))
                        .build())
                .toList()
                : new java.util.ArrayList<>();

        return ProjectResponse.builder()
                .id(p.getId())
                .clientId(clientId)
                .managerId(p.getManagerId())
                .name(p.getName())
                .description(p.getDescription())
                .type(p.getType())
                .startDate(p.getStartDate())
                .deadline(p.getDeadline())
                .status(p.getStatus())
                .team(teamDto)
                .build();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank())
            return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}