package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.FinanceSummaryResponse;
import com.freelance.freelancepm.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/finance")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryResponse> getFinanceSummary(
            @RequestParam(value = "period", defaultValue = "month") String period) {
        // period can be "week", "month", or "year"
        return ResponseEntity.ok(financeService.getFinanceSummary(period));
    }
}
