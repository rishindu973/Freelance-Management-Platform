package com.freelance.freelancepm.scheduler;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.service.InvoiceStatusTransitionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceSchedulerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceStatusTransitionService transitionService;

    @InjectMocks
    private InvoiceScheduler invoiceScheduler;

    @Test
    void markOverdueInvoices_ShouldMarkPastDueSentInvoices() {
        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setId(1);
        overdueInvoice.setStatus(InvoiceStatus.SENT);
        overdueInvoice.setDueDate(LocalDate.now().minusDays(1)); // Past due

        Invoice futureInvoice = new Invoice();
        futureInvoice.setId(2);
        futureInvoice.setStatus(InvoiceStatus.SENT);
        futureInvoice.setDueDate(LocalDate.now().plusDays(5)); // Future due

        when(invoiceRepository.findByStatus(InvoiceStatus.SENT))
                .thenReturn(List.of(overdueInvoice, futureInvoice));

        invoiceScheduler.markOverdueInvoices();

        // Should only transition the overdue invoice
        verify(transitionService).validateAndTransition(overdueInvoice, InvoiceStatus.OVERDUE, "system_scheduler");
        verify(invoiceRepository).save(overdueInvoice);

        verify(transitionService, never()).validateAndTransition(eq(futureInvoice), any(), any());
    }
}
