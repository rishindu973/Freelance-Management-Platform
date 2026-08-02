package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.EmailLog;
import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.entity.User;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.EmailLogRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailDispatcherServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private EmailDispatcherService dispatcherService;

    private Invoice mockInvoice;
    private Manager mockManager;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setId(1);
        client.setName("Test Client");

        mockInvoice = new Invoice();
        mockInvoice.setId(100);
        mockInvoice.setInvoiceNumber("INV-2026-0001");
        mockInvoice.setClient(client);
        mockInvoice.setTotal(new BigDecimal("500.00"));
        mockInvoice.setStatus(InvoiceStatus.FINAL);

        mockManager = new Manager();
        mockManager.setCompanyName("Manager Co");
        mockManager.setContactNumber("123456789");
        mockManager.setLogoUrl("http://logo.com/img.png");
        mockManager.setAddress("123 Main St");
        User user = new User();
        user.setEmail("manager@example.com");
        mockManager.setUser(user);

        lenient().when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(i -> i.getArguments()[0]);
        lenient().when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);
    }

    // ── Single Recipient: Success ─────────────────────────────────────

    @Test
    void dispatch_SingleRecipient_Success_StatusSent() {
        // Act
        dispatcherService.dispatchInvoices(
                mockInvoice, mockManager,
                List.of("client@example.com"),
                "2026-06-01",
                "pdf".getBytes(), "Invoice_INV.pdf"
        );

        // Assert: email sent once, no retries
        verify(emailService, times(1)).sendInvoiceEmail(
                eq("client@example.com"), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                any(), anyString()
        );

        // Assert: one SUCCESS log persisted
        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(logCaptor.capture());
        assertEquals(EmailLog.LogStatus.SUCCESS, logCaptor.getValue().getStatus());
        assertEquals(1, logCaptor.getValue().getAttemptNumber());

        // Assert: invoice marked SENT
        assertEquals(InvoiceStatus.SENT, mockInvoice.getStatus());
        assertNull(mockInvoice.getFailureReason());
        assertNotNull(mockInvoice.getLastSentAt());
    }

    // ── Multiple Recipients: All Succeed ──────────────────────────────

    @Test
    void dispatch_MultipleRecipients_AllSuccess() {
        List<String> recipients = List.of("a@test.com", "b@test.com", "c@test.com");

        dispatcherService.dispatchInvoices(
                mockInvoice, mockManager, recipients,
                "2026-06-01", "pdf".getBytes(), "Invoice.pdf"
        );

        // Assert: each recipient gets exactly one call (no retries needed)
        verify(emailService, times(3)).sendInvoiceEmail(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                any(), anyString()
        );

        // Assert: 3 SUCCESS logs
        verify(emailLogRepository, times(3)).save(argThat(log ->
                log.getStatus() == EmailLog.LogStatus.SUCCESS
        ));

        assertEquals(InvoiceStatus.SENT, mockInvoice.getStatus());
    }

    // ── Single Recipient: Failure After 3 Retries ─────────────────────

    @Test
    void dispatch_SingleRecipient_AllRetriesFail_StatusFailed() {
        // Arrange: email always fails
        doThrow(new RuntimeException("SMTP connection refused"))
                .when(emailService).sendInvoiceEmail(
                        anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        any(), anyString()
                );

        // Act
        dispatcherService.dispatchInvoices(
                mockInvoice, mockManager,
                List.of("bad@test.com"),
                "2026-06-01", "pdf".getBytes(), "Invoice.pdf"
        );

        // Assert: exactly 3 attempts
        verify(emailService, times(3)).sendInvoiceEmail(
                eq("bad@test.com"), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                any(), anyString()
        );

        // Assert: 3 FAILURE logs (one per attempt)
        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository, times(3)).save(logCaptor.capture());
        List<EmailLog> logs = logCaptor.getAllValues();
        for (int i = 0; i < 3; i++) {
            assertEquals(EmailLog.LogStatus.FAILURE, logs.get(i).getStatus());
            assertEquals(i + 1, logs.get(i).getAttemptNumber());
            assertEquals("SMTP connection refused", logs.get(i).getErrorMessage());
        }

        // Assert: invoice marked FAILED with reason
        assertEquals(InvoiceStatus.FAILED, mockInvoice.getStatus());
        assertTrue(mockInvoice.getFailureReason().contains("bad@test.com"));
    }

    // ── Retry Success: Fails Twice, Succeeds Third ────────────────────

    @Test
    void dispatch_SingleRecipient_FailTwice_SucceedThird() {
        // Arrange: fail first two, succeed on third
        doThrow(new RuntimeException("Timeout"))
                .doThrow(new RuntimeException("Timeout"))
                .doNothing()
                .when(emailService).sendInvoiceEmail(
                        anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        any(), anyString()
                );

        // Act
        dispatcherService.dispatchInvoices(
                mockInvoice, mockManager,
                List.of("retry@test.com"),
                "2026-06-01", "pdf".getBytes(), "Invoice.pdf"
        );

        // Assert: 3 total calls
        verify(emailService, times(3)).sendInvoiceEmail(
                eq("retry@test.com"), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                any(), anyString()
        );

        // Assert: 2 FAILURE + 1 SUCCESS log
        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository, times(3)).save(logCaptor.capture());
        List<EmailLog> logs = logCaptor.getAllValues();
        assertEquals(EmailLog.LogStatus.FAILURE, logs.get(0).getStatus());
        assertEquals(EmailLog.LogStatus.FAILURE, logs.get(1).getStatus());
        assertEquals(EmailLog.LogStatus.SUCCESS, logs.get(2).getStatus());

        // Assert: invoice marked SENT (partial retry succeeded)
        assertEquals(InvoiceStatus.SENT, mockInvoice.getStatus());
    }

    // ── Mixed: One Recipient Succeeds, One Fails ──────────────────────

    @Test
    void dispatch_MultipleRecipients_PartialFailure_StatusFailed() {
        // Arrange: first recipient succeeds, second always fails
        doNothing() // success for first recipient
                .doThrow(new RuntimeException("DNS failure"))
                .doThrow(new RuntimeException("DNS failure"))
                .doThrow(new RuntimeException("DNS failure"))
                .when(emailService).sendInvoiceEmail(
                        anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        any(), anyString()
                );

        // Act
        dispatcherService.dispatchInvoices(
                mockInvoice, mockManager,
                List.of("good@test.com", "bad@test.com"),
                "2026-06-01", "pdf".getBytes(), "Invoice.pdf"
        );

        // Assert: 1 call for good + 3 retries for bad = 4 total
        verify(emailService, times(4)).sendInvoiceEmail(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                any(), anyString()
        );

        // Assert: FAILED status since not all succeeded
        assertEquals(InvoiceStatus.FAILED, mockInvoice.getStatus());
        assertTrue(mockInvoice.getFailureReason().contains("bad@test.com"));
    }

    // ── Idempotency: lastSentAt is always set ─────────────────────────

    @Test
    void dispatch_SetsLastSentAtTimestamp() {
        assertNull(mockInvoice.getLastSentAt());

        dispatcherService.dispatchInvoices(
                mockInvoice, mockManager,
                List.of("client@example.com"),
                "2026-06-01", "pdf".getBytes(), "Invoice.pdf"
        );

        assertNotNull(mockInvoice.getLastSentAt());
        verify(invoiceRepository).save(mockInvoice);
    }
}
