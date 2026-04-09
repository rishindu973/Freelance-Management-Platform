package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.EmailLog;
import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.repository.EmailLogRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailDispatcherService {

    private final EmailService emailService;
    private final EmailLogRepository emailLogRepository;
    private final InvoiceRepository invoiceRepository;

    private static final int MAX_RETRIES = 3;

    @Async("emailTaskExecutor")
    public void dispatchInvoices(
            Invoice invoice,
            Manager manager,
            List<String> recipients,
            String dueDate,
            byte[] pdfBytes,
            String filename
    ) {
        log.info("Background dispatch started for invoice: {}", invoice.getInvoiceNumber());
        invoice.setLastSentAt(java.time.LocalDateTime.now());

        boolean allSucceeded = true;
        StringBuilder finalFailureReason = new StringBuilder();

        for (String recipient : recipients) {
            boolean success = sendWithRetry(
                    recipient, 
                    invoice, 
                    manager, 
                    dueDate, 
                    pdfBytes, 
                    filename
            );
            
            if (!success) {
                allSucceeded = false;
                finalFailureReason.append("Failed to send to ").append(recipient).append(". ");
            }
        }

        if (allSucceeded) {
            invoice.setStatus(Invoice.Status.SENT);
            invoice.setFailureReason(null);
        } else {
            invoice.setStatus(Invoice.Status.FAILED);
            invoice.setFailureReason(finalFailureReason.toString().trim());
        }

        invoiceRepository.save(invoice);
        log.info("Background dispatch finished for invoice: {}. Final Status: {}", 
                invoice.getInvoiceNumber(), invoice.getStatus());
    }

    private boolean sendWithRetry(
            String recipient,
            Invoice invoice,
            Manager manager,
            String dueDate,
            byte[] pdfBytes,
            String filename
    ) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Attempt {} for recipient: {}", attempt, recipient);
                
                emailService.sendInvoiceEmail(
                        recipient,
                        invoice.getClient().getName(),
                        invoice.getInvoiceNumber(),
                        invoice.getTotal().toString(),
                        "$", 
                        dueDate,
                        manager.getCompanyName(),
                        manager.getUser().getEmail(),
                        manager.getContactNumber(),
                        manager.getLogoUrl(),
                        manager.getAddress(),
                        "Direct Bank Transfer", 
                        pdfBytes,
                        filename
                );

                logEmailAttempt(invoice.getId(), recipient, EmailLog.LogStatus.SUCCESS, attempt, null);
                return true;

            } catch (Exception e) {
                log.error("Attempt {} failed for {}: {}", attempt, recipient, e.getMessage());
                logEmailAttempt(invoice.getId(), recipient, EmailLog.LogStatus.FAILURE, attempt, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    long backoff = (long) Math.pow(2, attempt - 1) * 1000; // 1s, 2s, 4s
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private void logEmailAttempt(Integer invoiceId, String recipient, EmailLog.LogStatus status, int attempt, String error) {
        EmailLog emailLog = EmailLog.builder()
                .invoiceId(invoiceId)
                .recipient(recipient)
                .status(status)
                .attemptNumber(attempt)
                .errorMessage(error)
                .build();
        emailLogRepository.save(emailLog);
    }
}
