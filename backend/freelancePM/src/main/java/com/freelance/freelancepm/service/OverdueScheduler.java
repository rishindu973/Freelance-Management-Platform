package com.freelance.freelancepm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueScheduler {

    private final InvoiceRepository invoiceRepository;
    private final PaymentService paymentService;

    // SCRUM 51: Detect and flag overdue payments
    @Scheduled(cron = "0 0 0 * * ?") // runs daily at midnight
    public void checkOverdueInvoices() {

        List<Invoice> invoices = invoiceRepository.findAll();

        for (Invoice invoice : invoices) {

            BigDecimal remaining = paymentService.getRemainingBalance(invoice.getId());

            if (invoice.getDueDate().isBefore(LocalDate.now())
                    && remaining.compareTo(BigDecimal.ZERO) > 0) {

                invoice.setStatus("OVERDUE");

                // SCRUM 51: Send reminder (future implementation)
            }
        }

        invoiceRepository.saveAll(invoices);
    }
}