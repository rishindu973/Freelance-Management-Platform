package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ProjectCreateRequest;
import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.dto.ProjectUpdateRequest;
import com.freelance.freelancepm.service.ProjectService;
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
public class ProjectController {

    private final ProjectService projectService;

    // TEMP: until JWT exists
    private Long requireManagerId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("Missing X-Manager-Id header");
        }
        return Long.parseLong(headerValue.trim());
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
            @Valid @RequestBody ProjectCreateRequest req
    ) {
        Long managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.create(managerId, req));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(
            @RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Long managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.list(managerId, status, clientId, search, from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> get(
            @RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
            @PathVariable Long id
    ) {
        Long managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.get(managerId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
            @PathVariable Long id,
            @RequestBody ProjectUpdateRequest req
    ) {
        Long managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(projectService.update(managerId, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
            @PathVariable Long id
    ) {
        Long managerId = requireManagerId(managerHeader);
        projectService.delete(managerId, id);
        return ResponseEntity.noContent().build();
    }
}