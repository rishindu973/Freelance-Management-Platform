package com.freelance.freelancepm.scheduler;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.service.InvoiceStatusTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceScheduler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusTransitionService transitionService;

    // Run every day at midnight (00:00)
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueInvoices() {
        log.info("Starting scheduled task: Checking for overdue invoices");
        
        LocalDate today = LocalDate.now();
        
        List<Invoice> sentInvoices = invoiceRepository.findByStatus(InvoiceStatus.SENT);
        int updatedCount = 0;
        
        for (Invoice invoice : sentInvoices) {
            if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(today)) {
                try {
                    transitionService.validateAndTransition(invoice, InvoiceStatus.OVERDUE, "system_scheduler");
                    invoiceRepository.save(invoice);
                    updatedCount++;
                } catch (Exception e) {
                    log.error("Failed to mark invoice {} as overdue", invoice.getId(), e);
                }
            }
        }
        
        log.info("Scheduled task finished. Marked {} invoices as OVERDUE.", updatedCount);
    }
}
