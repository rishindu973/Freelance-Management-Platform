package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.SendInvoiceRequest;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.service.InvoicePdfService;
import com.freelance.freelancepm.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerSendTest {

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private InvoicePdfService invoicePdfService;

    @InjectMocks
    private InvoiceController invoiceController;

    // ── Valid Requests ──────────────────────────────────────────────────

    @Test
    void sendInvoice_SingleRecipient_Returns200() {
        SendInvoiceRequest request = new SendInvoiceRequest(List.of("client@example.com"));

        ResponseEntity<Void> response = invoiceController.sendInvoice(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(invoiceService).sendInvoice(eq(1), any(SendInvoiceRequest.class));
    }

    @Test
    void sendInvoice_MultipleRecipients_Returns200() {
        SendInvoiceRequest request = new SendInvoiceRequest(
                List.of("a@test.com", "b@test.com", "c@test.com")
        );

        ResponseEntity<Void> response = invoiceController.sendInvoice(42, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(invoiceService).sendInvoice(eq(42), any(SendInvoiceRequest.class));
    }

    // ── Service Layer Exceptions ──────────────────────────────────────

    @Test
    void sendInvoice_InvoiceNotFound_ThrowsNotFoundException() {
        SendInvoiceRequest request = new SendInvoiceRequest(List.of("client@test.com"));

        doThrow(new NotFoundException("Invoice not found"))
                .when(invoiceService).sendInvoice(eq(999), any(SendInvoiceRequest.class));

        assertThrows(NotFoundException.class, () ->
                invoiceController.sendInvoice(999, request)
        );
    }

    @Test
    void sendInvoice_ManagerNotFound_ThrowsIllegalStateException() {
        SendInvoiceRequest request = new SendInvoiceRequest(List.of("client@test.com"));

        doThrow(new IllegalStateException("Manager details not found"))
                .when(invoiceService).sendInvoice(eq(1), any(SendInvoiceRequest.class));

        assertThrows(IllegalStateException.class, () ->
                invoiceController.sendInvoice(1, request)
        );
    }

    // ── Idempotency ──────────────────────────────────────────────────

    @Test
    void sendInvoice_AlreadySent_ServiceHandlesIdempotency() {
        SendInvoiceRequest request = new SendInvoiceRequest(List.of("client@test.com"));

        // Service just returns without doing anything for already-sent invoices
        doNothing().when(invoiceService).sendInvoice(eq(1), any(SendInvoiceRequest.class));

        ResponseEntity<Void> response = invoiceController.sendInvoice(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(invoiceService, times(1)).sendInvoice(eq(1), any(SendInvoiceRequest.class));
    }
}
