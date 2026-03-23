package com.freelance.freelancepm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freelance.freelancepm.entity.Task;
import com.freelance.freelancepm.service.TaskService;
import com.freelance.freelancepm.service.IManagerService;
import com.freelance.freelancepm.dto.TaskRequest;
import org.springframework.web.bind.annotation.CrossOrigin;

import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<Task> assign(
            java.security.Principal principal,
            @RequestBody TaskRequest request) {

        Integer managerId = requireManagerId(principal);

        Task task = taskService.assignTask(
                request.getFreelancerEmail(),
                request.getTitle(),
                request.getDescription(),
                managerId);
        return ResponseEntity.ok(task);
    }

}
