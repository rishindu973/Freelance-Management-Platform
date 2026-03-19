package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.entity.ProjectStatus;
import com.freelance.freelancepm.entity.ProgressStatus;
import com.freelance.freelancepm.dto.ProjectCreateRequest;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.ProjectUpdateRequest;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.repository.FreelancerRepository;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final FreelancerRepository freelancerRepository;

    // ----------------- Manager Methods -----------------

    public ProjectResponse create(Integer managerId, ProjectCreateRequest req) {
        Project p = Project.builder()
                .managerId(managerId)
                .client(req.getClient())
                .name(req.getName())
                .description(req.getDescription())
                .type(req.getType())
                .startDate(req.getStartDate())
                .deadline(req.getDeadline())
                .status(req.getStatus() != null ? ProjectStatus.valueOf(req.getStatus()) : ProjectStatus.ACTIVE)
                .progressStatus(req.getProgressStatus() != null ? ProgressStatus.valueOf(req.getProgressStatus()) : ProgressStatus.NOT_STARTED)
                .progressPercentage(req.getProgressPercentage() != null ? req.getProgressPercentage() : 0)
                .archived(false)
                .build();

        updateDeadlineFlags(p);

        return toResponse(p);
    }

    public List<ProjectResponse> list(Integer managerId, String status, Integer clientId, String search, LocalDate from, LocalDate to, Boolean isCritical) {
        // Use Specification / filters here
        List<Project> projects = projectRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        projects.forEach(this::updateDeadlineFlags);

        return projects.stream().map(this::toResponse).toList();
    }

    public ProjectResponse get(Integer managerId, Integer projectId) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        updateDeadlineFlags(p);
        return toResponse(p);
    }

    public ProjectResponse update(Integer managerId, Integer projectId, ProjectUpdateRequest req) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (req.getClient() != null) p.setClient(req.getClient());
        if (req.getName() != null) p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getType() != null) p.setType(req.getType());
        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getDeadline() != null) p.setDeadline(req.getDeadline());
        if (req.getStatus() != null) p.setStatus(ProjectStatus.valueOf(req.getStatus()));
        if (req.getProgressStatus() != null) p.setProgressStatus(ProgressStatus.valueOf(req.getProgressStatus()));
        if (req.getProgressPercentage() != null) p.setProgressPercentage(req.getProgressPercentage());

        updateDeadlineFlags(p);

        return toResponse(p);
    }

    @Transactional
    public ProjectResponse updateTeam(Integer managerId, Integer projectId, List<Integer> freelancerIds) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        p.setTeam(freelancerRepository.findAllById(freelancerIds));
        return toResponse(p);
    }

    public void delete(Integer managerId, Integer projectId) {
        Project p = projectRepository.findByIdAndManagerId(projectId, managerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        projectRepository.delete(p);
    }

    // ----------------- Deadline & Progress Methods -----------------

    private void updateDeadlineFlags(Project p) {
        LocalDate today = LocalDate.now();
        if (p.getDeadline() != null) {
            p.setOverdue(p.getDeadline().isBefore(today) && p.getStatus() != ProjectStatus.COMPLETED);
            p.setUrgent(!p.getOverdue() && ChronoUnit.DAYS.between(today, p.getDeadline()) <= 7);
        }
    }

    public ProjectResponse updateProgress(Integer projectId, String progressStatus, Integer percentage) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (progressStatus != null) p.setProgressStatus(ProgressStatus.valueOf(progressStatus));
        if (percentage != null) p.setProgressPercentage(percentage);

        return toResponse(p);
    }

    public ProjectResponse completeProject(Integer projectId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (p.getProgressPercentage() < 100) throw new IllegalStateException("Pending items exist");

        p.setStatus(ProjectStatus.COMPLETED);
        p.setArchived(true);

        return toResponse(p);
    }

    public ProjectResponse reopenProject(Integer projectId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        p.setStatus(ProjectStatus.ACTIVE);
        p.setArchived(false);

        return toResponse(p);
    }

    // ----------------- Client Methods -----------------

    public List<ProjectResponse> getProjectsByClient(Integer clientId, String status, LocalDate from, LocalDate to) {
        List<Project> projects = (status != null || from != null || to != null)
                ? projectRepository.filterProjects(clientId, status, from, to)
                : projectRepository.findByClientId(clientId);

        projects.forEach(this::updateDeadlineFlags);
        return projects.stream().map(this::toResponse).toList();
    }

    // ----------------- Mapper -----------------

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
                .status(p.getStatus().name())
                .progressStatus(p.getProgressStatus() != null ? p.getProgressStatus().name() : null)
                .progressPercentage(p.getProgressPercentage())
                .urgent(p.getUrgent())
                .overdue(p.getOverdue())
                .archived(p.getArchived())
                .team(p.getTeam())
                .build();
    }
}