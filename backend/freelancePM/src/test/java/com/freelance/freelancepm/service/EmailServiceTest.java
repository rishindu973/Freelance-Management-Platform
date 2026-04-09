package com.freelance.freelancepm.service;

import com.freelance.freelancepm.email.EmailTemplateService;
import com.freelance.freelancepm.exception.EmailException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTemplateService templateService;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@freelanceflow.com");
    }

    @Test
    void sendInvoiceEmail_Success() throws Exception {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        List<String> toEmails = List.of("client@example.com");
        String subject = "Your Invoice";
        String body = "Please find attached your invoice.";
        byte[] pdfBytes = "dummy pdf content".getBytes();
        String filename = "invoice.pdf";

        // Act
        emailService.sendInvoiceEmail(toEmails, subject, body, pdfBytes, filename);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendInvoiceEmail_WithManagerDetails_Success() throws Exception {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateService.generateInvoiceEmailBody(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("<html>Manager Invoice</html>");

        // Act
        emailService.sendInvoiceEmail(
                "client@example.com",
                "John Client",
                "INV-001",
                "100.00",
                "$",
                "2026-05-01",
                "Manager Co",
                "manager@example.com",
                "123456",
                "http://logo.com",
                "Address 1",
                "Bank: XYZ",
                "pdf".getBytes(),
                "invoice.pdf"
        );

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendInvoiceEmail_Failure_ThrowsEmailException() throws Exception {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(MimeMessage.class));

        List<String> toEmails = List.of("client@example.com");
        
        // Act & Assert
        assertThrows(EmailException.class, () -> 
            emailService.sendInvoiceEmail(toEmails, "Sub", "Body", new byte[0], "file.pdf")
        );
    }

    @Test
    void sendWelcomeEmail_Success() throws Exception {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateService.welcomeTemplate(anyString(), anyString())).thenReturn("<html>Welcome</html>");

        // Act
        emailService.sendWelcomeEmail("newuser@example.com", "pass123");

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }
}
