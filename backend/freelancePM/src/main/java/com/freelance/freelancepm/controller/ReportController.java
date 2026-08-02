package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ReportResponse;
import com.freelance.freelancepm.service.ReportPdfService;
import com.freelance.freelancepm.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportPdfService reportPdfService;
    private final com.freelance.freelancepm.service.IManagerService managerService;

    private Integer requireManagerId(java.security.Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return managerService.getManagerIdByEmail(principal.getName());
    }

    @GetMapping
    public ResponseEntity<ReportResponse> getReport(
            java.security.Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Integer managerId = requireManagerId(principal);

        if (endDate == null)
            endDate = LocalDate.now();
        if (startDate == null)
            startDate = endDate.minusDays(30);

        return ResponseEntity.ok(reportService.getReport(managerId, startDate, endDate));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport(
            java.security.Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Integer managerId = requireManagerId(principal);

        if (endDate == null)
            endDate = LocalDate.now();
        if (startDate == null)
            startDate = endDate.minusDays(30);

        try {
            byte[] pdfContent = reportPdfService.generateReportPdf(managerId, startDate, endDate);

            String fileName = "FreelanceFlow_Report_" + startDate + "_to_" + endDate + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);
        } catch (Exception e) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("error_log.txt"))) {
                e.printStackTrace(pw);
            } catch (Exception ignored) {
            }
            throw new RuntimeException("PDF Generation Error: " + e.getMessage(), e);
        }
    }
}
