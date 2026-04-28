package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.DashboardResponse;
import com.freelance.freelancepm.dto.WorkSummaryResponse;
import com.freelance.freelancepm.service.IDashboardService;
import com.freelance.freelancepm.service.IManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class DashboardController {

    private final IDashboardService dashboardService;
    private final IManagerService managerService;

    // Use JWT Principal
    private Integer requireManagerId(java.security.Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return managerService.getManagerIdByEmail(principal.getName());
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            java.security.Principal principal,
            @RequestParam(value = "dueSoonDays", defaultValue = "7") int dueSoonDays,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        Integer managerId = requireManagerId(principal);
        return ResponseEntity.ok(dashboardService.getDashboard(managerId, dueSoonDays, limit));
    }

    @GetMapping("/work-summary")
    public ResponseEntity<WorkSummaryResponse> getWorkSummary(java.security.Principal principal) {
        Integer managerId = requireManagerId(principal);
        return ResponseEntity.ok(dashboardService.getWorkSummary(managerId));
    }
}