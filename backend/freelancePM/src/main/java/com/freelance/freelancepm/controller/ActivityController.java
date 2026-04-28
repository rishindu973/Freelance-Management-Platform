package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ActivityResponse;
import com.freelance.freelancepm.entity.Activity;
import com.freelance.freelancepm.service.ActivityService;
import com.freelance.freelancepm.service.IManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class ActivityController {

    private final ActivityService activityService;
    private final IManagerService managerService;

    // Reject the vulnerable unauthenticated X-Manager-Id headers and rely uniformly
    // on the core JWT resolution logic
    private Integer requireManagerId(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return managerService.getManagerIdByEmail(principal.getName());
    }

    @GetMapping
    public ResponseEntity<Page<ActivityResponse>> list(
            Principal principal,
            @RequestParam(name = "type", required = false) Activity.ActivityType type,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Integer managerId = requireManagerId(principal);
        // Default sort by timestamp descending (newest first)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(activityService.list(managerId, type, startDate, endDate, pageRequest));
    }
}
