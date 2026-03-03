package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.DashboardResponse;
import com.freelance.freelancepm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // TEMP: until JWT exists (same pattern as ProjectController)
    private Integer requireManagerId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("Missing X-Manager-Id header");
        }
        return Integer.parseInt(headerValue.trim());
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
            @RequestParam(defaultValue = "7") int dueSoonDays,
            @RequestParam(defaultValue = "5") int limit) {
        Integer managerId = requireManagerId(managerHeader);
        return ResponseEntity.ok(dashboardService.getDashboard(managerId, dueSoonDays, limit));
    }
}