package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ActivityResponse;
import com.freelance.freelancepm.entity.Activity;
import com.freelance.freelancepm.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    // TEMP: until JWT exists
    private Long requireManagerId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("Missing X-Manager-Id header");
        }
        return Long.parseLong(headerValue.trim());
    }

    @GetMapping
    public ResponseEntity<Page<ActivityResponse>> list(
            @RequestHeader(value = "X-Manager-Id", required = false) String managerHeader,
            @RequestParam(required = false) Activity.ActivityType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long managerId = requireManagerId(managerHeader);
        // Default sort by timestamp descending (newest first)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(activityService.list(managerId, type, startDate, endDate, pageRequest));
    }
}
