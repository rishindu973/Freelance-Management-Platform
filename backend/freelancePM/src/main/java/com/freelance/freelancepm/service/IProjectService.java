package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.ProjectCreateRequest;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.ProjectUpdateRequest;

import java.time.LocalDate;
import java.util.List;

public interface IProjectService {
    ProjectResponse create(Integer managerId, ProjectCreateRequest req);

    List<ProjectResponse> list(Integer managerId, String status, Integer clientId, String search, LocalDate from,
            LocalDate to, Boolean isCritical);

    ProjectResponse get(Integer managerId, Integer projectId);

    ProjectResponse update(Integer managerId, Integer projectId, ProjectUpdateRequest req);

    ProjectResponse updateTeam(Integer managerId, Integer projectId, List<Integer> freelancerIds);

    void delete(Integer managerId, Integer projectId);

    ProjectResponse updateProgress(Integer projectId, String progressStatus, Integer percentage);

    ProjectResponse completeProject(Integer projectId);

    ProjectResponse reopenProject(Integer projectId);

    List<ProjectResponse> getProjectsForFreelancer(Integer freelancerId);
}
