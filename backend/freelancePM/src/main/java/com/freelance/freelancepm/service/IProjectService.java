package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.ProjectCreateRequest;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.ProjectUpdateRequest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProjectService {
    ProjectResponse create(Integer managerId, ProjectCreateRequest req);

    Page<ProjectResponse> list(Integer managerId, String status, Integer clientId, String search, LocalDate from,
            LocalDate to, Boolean isCritical, Pageable pageable);

    ProjectResponse get(Integer managerId, Integer projectId);

    ProjectResponse update(Integer managerId, Integer projectId, ProjectUpdateRequest req);

    ProjectResponse updateTeam(Integer managerId, Integer projectId, List<Integer> freelancerIds);

    void delete(Integer managerId, Integer projectId);

    ProjectResponse updateProgress(Integer projectId, String progressStatus, Integer percentage);

    ProjectResponse completeProject(Integer projectId);

    ProjectResponse reopenProject(Integer projectId);

    List<ProjectResponse> getProjectsForFreelancer(Integer freelancerId);
}
