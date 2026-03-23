package com.freelance.freelancepm.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.freelance.freelancepm.email.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender javaMailSender;
    private final EmailTemplateService templateService;

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Override
    public void sendWelcomeEmail(String to, String temporaryPassword) {
        String body = templateService.welcomeTemplate(to, temporaryPassword);

        try {
            sendEmail(to, "Your FreelanceFlow Credentials", body);
        } catch (Exception e) {
            // Log missing SMTP server configuration exception, but don't crash the server.
            System.err.println("Could not send welcome email to " + to + ". Check your SMTP config.");
            System.err.println("Raw password for testing: " + temporaryPassword);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        String body = "<p>Use this token to reset your password: " + resetToken + "</p>";

        try {
            sendEmail(to, "Password Reset Request", body);
        } catch (Exception e) {
            System.err.println("Could not send reset email to " + to + ". Check your SMTP config.");
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

        sendEmail(to, "Verify Your FreelanceFlow Account", body);
    }

    public void sendTaskAssignmentEmail(String to, String name, String task) {
        String body = templateService.taskTemplate(name, task);
        sendEmail(to, "New Task Assigned", body);
    }

    public void sendEmail(String to, String subject, String htmlContent) {
        Email from = new Email(fromEmail);
        Email reciptent = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, reciptent, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sg.api(request);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    System.out.println("Email sent successfully to: " + to);
                    return;
                }
                System.err.println("Attempt: " + attempt + " to send email to " + to + " Failed! " + "Status: "
                        + response.getStatusCode());
            } catch (IOException e) {
                System.err.println("Attempt " + attempt + " error: " + e.getMessage());
            }
        }
        System.err.println("Failed to send email to " + to + " after " + maxRetries + " attempts.");
    }
}
