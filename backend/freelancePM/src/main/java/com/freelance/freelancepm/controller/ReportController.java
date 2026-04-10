package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ReportResponse;
import com.freelance.freelancepm.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportResponse> getReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (endDate == null)
            endDate = LocalDate.now();
        if (startDate == null)
            startDate = endDate.minusDays(30);

        return ResponseEntity.ok(reportService.getReport(startDate, endDate));
    }
}
