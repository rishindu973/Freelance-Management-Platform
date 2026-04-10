package com.freelance.freelancepm.service;

import com.freelance.freelancepm.email.EmailTemplateService;
import com.freelance.freelancepm.exception.EmailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Value("${mail.from:noreply@freelanceflow.com}")
    private String fromEmail;

    @Override
    public void sendWelcomeEmail(String to, String temporaryPassword) {
        String body = templateService.welcomeTemplate(to, temporaryPassword);
        try {
            sendEmail(to, "Your FreelanceFlow Credentials", body);
        } catch (EmailException e) {
            System.err.println("Could not send welcome email to " + to + ". " + e.getMessage());
            System.err.println("Raw password for testing: " + temporaryPassword);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

        String body = "<h2>Password Reset Request</h2>" +
                "<p>We received a request to reset your password. Click the button below to choose a new one:</p>" +
                "<br>" +
                "<a href=\"" + resetLink
                + "\" style=\"background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block; font-weight: bold;\">Reset Password</a>"
                +
                "<br><br>" +
                "<p style=\"color: #666; font-size: 14px;\">If the button doesn't work, copy and paste this link into your browser:<br>"
                + resetLink + "</p>" +
                "<p style=\"color: #666; font-size: 14px;\">If you did not request a password reset, you can safely ignore this email. This link will expire in 1 hour.</p>";

        try {
            sendEmail(to, "Password Reset Request", body);
        } catch (EmailException e) {
            System.err.println("Could not send reset email to " + to + ". " + e.getMessage());
            System.err.println("Reset token for testing: " + resetToken);
        }
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationLink = "http://localhost:5173/verify?token=" + token;

        String body = "<h2>Verify Your Account</h2>" +
                "<p>Thank you for joining FreelanceFlow. Please click the button below to verify your email:</p>" +
                "<a href=\"" + verificationLink
                + "\" style=\"background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Verify Email</a>"
                +
                "<p>If the button doesn't work, copy and paste this link: " + verificationLink + "</p>";

        try {
            sendEmail(to, "Verify Your FreelanceFlow Account", body);
        } catch (EmailException e) {
            System.err.println("Could not send verification email to " + to + ". " + e.getMessage());
            System.err.println("Verification token for testing: " + token);
        }
    }

    @Override
    public void sendInvoiceEmail(List<String> toEmails, String subject, String body, byte[] pdfBytes, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmails.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(body, true);

            helper.addAttachment(filename, new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            System.out.println("Invoice email sent successfully to: " + toEmails);
        } catch (Exception e) {
            throw new EmailException("Failed to send invoice email with attachment", e);
        }
    }

    @Override
    public void sendInvoiceEmail(
            String toEmail,
            String clientName,
            String invoiceNumber,
            String amount,
            String currencySymbol,
            String dueDate,
            String managerCompanyName,
            String managerEmail,
            String managerPhone,
            String managerLogoUrl,
            String managerAddress,
            String paymentInstructions,
            byte[] pdfBytes,
            String filename
    ) {
        String subject = "Invoice " + invoiceNumber + " from " + managerCompanyName;
        String body = templateService.generateInvoiceEmailBody(
                clientName,
                invoiceNumber,
                amount,
                currencySymbol,
                dueDate,
                managerCompanyName,
                managerEmail,
                managerPhone,
                managerLogoUrl,
                managerAddress,
                paymentInstructions
        );
        sendInvoiceEmail(List.of(toEmail), subject, body, pdfBytes, filename);
    }

    public void sendTaskAssignmentEmail(String to, String name, String task) {
        String body = templateService.taskTemplate(name, task);
        try {
            sendEmail(to, "New Task Assigned", body);
        } catch (EmailException e) {
            System.err.println("Could not send task assignment email to " + to + ". " + e.getMessage());
        }
    }

    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            throw new EmailException("Failed to send email to " + to, e);
        }
    }
}
