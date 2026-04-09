package com.freelance.freelancepm.listener;

import com.freelance.freelancepm.event.InvoicePaidEvent;
import com.freelance.freelancepm.service.ProfitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfitCalculationListener {

    private final ProfitService profitService;
    // Potentially inject a CacheManager or DashboardService to update cached metrics.

    @Async
    @EventListener
    public void handleInvoicePaidEvent(InvoicePaidEvent event) {
        log.info("Received InvoicePaidEvent for invoice ID: {} with amount: {}", event.getInvoiceId(), event.getAmount());

        // Recalculate total income
        java.math.BigDecimal totalIncome = profitService.calculateTotalIncome();
        
        log.info("New Total Income recalculated: {}", totalIncome);
        // If we had a caching layer or a pre-materialized view for the dashboard,
        // we would update it here to avoid heavy DB queries continuously.
    }
}
