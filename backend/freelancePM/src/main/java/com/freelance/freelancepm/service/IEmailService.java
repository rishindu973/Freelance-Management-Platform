package com.freelance.freelancepm.service;

public interface IEmailService {
    void sendWelcomeEmail(String to, String temporaryPassword);

    void sendPasswordResetEmail(String to, String resetToken);

    void sendVerificationEmail(String to, String token);

    void sendInvoiceEmail(java.util.List<String> toEmails, String subject, String body, byte[] pdfBytes, String filename);

    void sendInvoiceEmail(
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
    );
}
