package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ProjectCreateRequest;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.ProjectUpdateRequest;
import com.freelance.freelancepm.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    private Integer requireManagerId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) throw new IllegalArgumentException("Missing X-Manager-Id header");
        return Integer.parseInt(headerValue.trim());
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
                                                  @RequestBody ProjectCreateRequest req) {
        Integer managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.create(managerId, req));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(@RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) Integer clientId,
                                                      @RequestParam(required = false) String search,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                      @RequestParam(required = false) Boolean isCritical) {
        Integer managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.list(managerId, status, clientId, search, from, to, isCritical));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> get(@RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
                                               @PathVariable Integer id) {
        Integer managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.get(managerId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
                                                  @PathVariable Integer id,
                                                  @RequestBody ProjectUpdateRequest req) {
        Integer managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.update(managerId, id, req));
    }

    @PutMapping("/{id}/team")
    public ResponseEntity<ProjectResponse> updateTeam(@RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
                                                      @PathVariable Integer id,
                                                      @RequestBody List<Integer> freelancerIds) {
        Integer managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.updateTeam(managerId, id, freelancerIds));
    }

    // ----------------- Progress -----------------
    @PutMapping("/{id}/progress")
    public ResponseEntity<ProjectResponse> updateProgress(@PathVariable Integer id,
                                                          @RequestParam String progressStatus,
                                                          @RequestParam Integer percentage) {
        return ResponseEntity.ok(projectService.updateProgress(id, progressStatus, percentage));
    }

    // ----------------- Complete / Reopen -----------------
    @PutMapping("/{id}/complete")
    public ResponseEntity<ProjectResponse> completeProject(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.completeProject(id));
    }

    @PutMapping("/{id}/reopen")
    public ResponseEntity<ProjectResponse> reopenProject(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.reopenProject(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
                                       @PathVariable Integer id) {
        Integer managerId = requireManagerId(managerHeader);
        projectService.delete(managerId, id);
        return ResponseEntity.noContent().build();
    }
}