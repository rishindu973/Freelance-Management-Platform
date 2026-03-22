package com.freelance.freelancepm.service;

public interface IEmailService {
    void sendWelcomeEmail(String to, String temporaryPassword);

    void sendPasswordResetEmail(String to, String resetToken);

    void sendVerificationEmail(String to, String token);
}