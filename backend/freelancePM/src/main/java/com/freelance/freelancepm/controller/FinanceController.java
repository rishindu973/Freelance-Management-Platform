package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.FinanceSummaryResponse;
import com.freelance.freelancepm.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/finance")
@RequiredArgsConstructor

public class FinanceController {

    private final FinanceService financeService;
    private final com.freelance.freelancepm.service.IManagerService managerService;

    private Integer requireManagerId(java.security.Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return managerService.getManagerIdByEmail(principal.getName());
    }

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryResponse> getFinanceSummary(
            java.security.Principal principal,
            @RequestParam(value = "period", defaultValue = "month") String period) {
        Integer managerId = requireManagerId(principal);
        // period can be "week", "month", or "year"
        return ResponseEntity.ok(financeService.getFinanceSummary(managerId, period));
    }
}
