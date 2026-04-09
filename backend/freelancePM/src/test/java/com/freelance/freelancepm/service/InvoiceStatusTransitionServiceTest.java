package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.exception.InvalidStatusTransitionException;
import com.freelance.freelancepm.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceStatusTransitionServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private InvoiceStatusTransitionService transitionService;

    @Test
    void validateAndTransition_DraftToSent_ShouldSucceed() {
        Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        Invoice updated = transitionService.validateAndTransition(invoice, InvoiceStatus.SENT, "tester");

        assertEquals(InvoiceStatus.SENT, updated.getStatus());
        verify(auditLogService).logStatusChange(1, InvoiceStatus.DRAFT, InvoiceStatus.SENT, "tester");
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void validateAndTransition_DraftToOverdue_ShouldThrowException() {
        Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setStatus(InvoiceStatus.DRAFT);

        assertThrows(InvalidStatusTransitionException.class, () -> {
            transitionService.validateAndTransition(invoice, InvoiceStatus.OVERDUE, "tester");
        });

        verifyNoInteractions(auditLogService);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void validateAndTransition_SentToPaid_ShouldSucceed() {
        Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setStatus(InvoiceStatus.SENT);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        Invoice updated = transitionService.validateAndTransition(invoice, InvoiceStatus.PAID, "tester");

        assertEquals(InvoiceStatus.PAID, updated.getStatus());
        verify(auditLogService).logStatusChange(1, InvoiceStatus.SENT, InvoiceStatus.PAID, "tester");
    }

    @Test
    void validateAndTransition_PaidToDraft_ShouldThrowException() {
        Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setStatus(InvoiceStatus.PAID);

        assertThrows(InvalidStatusTransitionException.class, () -> {
            transitionService.validateAndTransition(invoice, InvoiceStatus.DRAFT, "tester");
        });
    }
}
