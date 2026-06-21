package com.freelance.freelancepm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.freelance.freelancepm.service.TaskService;
import com.freelance.freelancepm.service.IManagerService;
import com.freelance.freelancepm.dto.TaskDTO;
import com.freelance.freelancepm.dto.TaskRequest;

import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class TaskController {
    private final TaskService taskService;
    private final IManagerService managerService;

    private Integer requireManagerId(java.security.Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return managerService.getManagerIdByEmail(principal.getName());
    }

    @PostMapping
    public ResponseEntity<TaskDTO> assign(
            java.security.Principal principal,
            @RequestBody TaskRequest request) {

        Integer managerId = requireManagerId(principal);

        TaskDTO task = taskService.assignTask(request, managerId);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByProject(@PathVariable Integer projectId) {
        return ResponseEntity.ok(taskService.getTasksByProjectId(projectId));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateStatus(
            @PathVariable Integer taskId,
            @RequestParam String status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, status));
    }
}
