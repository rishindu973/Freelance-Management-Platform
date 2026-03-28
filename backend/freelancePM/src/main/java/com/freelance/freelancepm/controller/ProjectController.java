package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ProjectCreateRequest;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.ProjectUpdateRequest;
import com.freelance.freelancepm.service.IProjectService;
import com.freelance.freelancepm.service.IManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class ProjectController {

    private final IProjectService projectService;
    private final IManagerService managerService;

    // Use JWT Principal
    private Integer requireManagerId(java.security.Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return managerService.getManagerIdByEmail(principal.getName());
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            java.security.Principal principal,
            @Valid @RequestBody ProjectCreateRequest req) {
        Integer managerId = requireManagerId(principal);
        return ResponseEntity.ok(projectService.create(managerId, req));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(
            java.security.Principal principal,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "clientId", required = false) Integer clientId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "isCritical", required = false) Boolean isCritical) {
        Integer managerId = requireManagerId(principal);
        return ResponseEntity.ok(projectService.list(managerId, status, clientId, search, from, to, isCritical));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> get(
            java.security.Principal principal,
            @PathVariable("id") Integer id) {
        Integer managerId = requireManagerId(principal);
        return ResponseEntity.ok(projectService.get(managerId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            java.security.Principal principal,
            @PathVariable("id") Integer id,
            @RequestBody ProjectUpdateRequest req) {
        Integer managerId = requireManagerId(principal);
        return ResponseEntity.ok(projectService.update(managerId, id, req));
    }

    @PutMapping("/{id}/team")
    public ResponseEntity<ProjectResponse> updateTeam(
            java.security.Principal principal,
            @PathVariable("id") Integer id,
            @RequestBody List<Integer> freelancerIds) {
        Integer managerId = requireManagerId(principal);
        return ResponseEntity.ok(projectService.updateTeam(managerId, id, freelancerIds));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            java.security.Principal principal,
            @PathVariable("id") Integer id) {
        Integer managerId = requireManagerId(principal);
        projectService.delete(managerId, id);
        return ResponseEntity.noContent().build();
    }

    // ----------------- Progress -----------------
    @PutMapping("/{id}/progress")
    public ResponseEntity<ProjectResponse> updateProgress(
            java.security.Principal principal,
            @PathVariable("id") Integer id,
            @RequestParam("progressStatus") String progressStatus,
            @RequestParam("percentage") Integer percentage) {
        requireManagerId(principal); // ensure auth
        return ResponseEntity.ok(projectService.updateProgress(id, progressStatus, percentage));
    }

    // ----------------- Complete / Reopen -----------------
    @PutMapping("/{id}/complete")
    public ResponseEntity<ProjectResponse> completeProject(
            java.security.Principal principal,
            @PathVariable("id") Integer id) {
        requireManagerId(principal); // ensure auth
        return ResponseEntity.ok(projectService.completeProject(id));
    }

    @PutMapping("/{id}/reopen")
    public ResponseEntity<ProjectResponse> reopenProject(
            java.security.Principal principal,
            @PathVariable("id") Integer id) {
        requireManagerId(principal); // ensure auth
        return ResponseEntity.ok(projectService.reopenProject(id));
    }
}