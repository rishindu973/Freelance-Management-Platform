package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.exception.InvalidStatusTransitionException;
import com.freelance.freelancepm.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InvoiceStatusTransitionService {

    private final InvoiceRepository invoiceRepository;
    private final AuditLogService auditLogService;

    // State machine configuration
    private static final Map<InvoiceStatus, Set<InvoiceStatus>> VALID_TRANSITIONS = new EnumMap<>(InvoiceStatus.class);

    static {
        // DRAFT can only be marked as SENT or directly PAID (if paying immediately)
        VALID_TRANSITIONS.put(InvoiceStatus.DRAFT, EnumSet.of(InvoiceStatus.SENT, InvoiceStatus.PAID));

        // SENT invoices can be PAID or eventually become OVERDUE
        VALID_TRANSITIONS.put(InvoiceStatus.SENT, EnumSet.of(InvoiceStatus.PAID, InvoiceStatus.OVERDUE));

        // OVERDUE invoices can be PAID (late)
        VALID_TRANSITIONS.put(InvoiceStatus.OVERDUE, EnumSet.of(InvoiceStatus.PAID));

        // PAID invoices are terminal in standard workflow (unless dealing with refunds
        // which aren't specified)
        VALID_TRANSITIONS.put(InvoiceStatus.PAID, EnumSet.noneOf(InvoiceStatus.class));

        // Legacy statuses
        VALID_TRANSITIONS.put(InvoiceStatus.FINAL, EnumSet.of(InvoiceStatus.SENT));
        VALID_TRANSITIONS.put(InvoiceStatus.FAILED, EnumSet.of(InvoiceStatus.SENT));
        VALID_TRANSITIONS.put(InvoiceStatus.PARTIALLY_PAID, EnumSet.of(InvoiceStatus.PAID));
        VALID_TRANSITIONS.put(InvoiceStatus.OVERPAID, EnumSet.noneOf(InvoiceStatus.class));
    }

    @Transactional
    public Invoice validateAndTransition(Invoice invoice, InvoiceStatus targetStatus, String userEmail) {
        InvoiceStatus currentStatus = invoice.getStatus();

        // If the status is naturally the same, do nothing
        if (currentStatus == targetStatus) {
            return invoice;
        }

        Set<InvoiceStatus> allowedNextStates = VALID_TRANSITIONS.getOrDefault(currentStatus,
                EnumSet.noneOf(InvoiceStatus.class));

        if (!allowedNextStates.contains(targetStatus)) {
            if (currentStatus == InvoiceStatus.PAID) {
                throw new InvalidStatusTransitionException("Cannot revert paid invoice. Create credit note instead.");
            }
            throw new InvalidStatusTransitionException(
                    String.format("Invalid status transition from %s to %s", currentStatus, targetStatus));
        }

        // Apply transition
        invoice.setStatus(targetStatus);

        // Audit log
        auditLogService.logStatusChange(invoice.getId(), currentStatus, targetStatus, userEmail);

        return invoiceRepository.save(invoice);
    }
}
