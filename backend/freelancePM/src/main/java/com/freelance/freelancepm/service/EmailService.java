package com.freelance.freelancepm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:admin@kingsman.com}")
    private String fromEmail;

    @Override
    public void sendWelcomeEmail(String to, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Welcome to Kingsman Freelance PM - Your Credentials");
        message.setText("Welcome!\n\nYour account has been created.\n\n" +
                "Username: " + to + "\n" +
                "Temporary Password: " + temporaryPassword + "\n\n" +
                "Please log in and change your password immediately.");

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            // Log missing SMTP server configuration exception, but don't crash the server.
            System.err.println("Could not send welcome email to " + to + ". Check your SMTP config.");
            System.err.println("Raw password for testing: " + temporaryPassword);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("You have requested a password reset.\n\n" +
                "Use the following token to reset your password:\n" + resetToken + "\n\n" +
                "If you did not request this, please ignore this email.");

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            System.err.println("Could not send reset email to " + to + ". Check your SMTP config.");
            System.err.println("Reset token for testing: " + resetToken);
        }
    }
}
