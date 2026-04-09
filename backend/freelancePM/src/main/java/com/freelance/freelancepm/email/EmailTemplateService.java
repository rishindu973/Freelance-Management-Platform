package com.freelance.freelancepm.email;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    private static final String PLATFORM_NAME = "FreelanceFlow";

    public String taskTemplate(String name, String task) {
        return "<h2>Hello " + name + "</h2><p>Task: " + task + "</p>";
    }

    public String projectTemplate(String name, String project) {
        return "<h2>Hello " + name + "</h2><p>Project: " + project + "</p>";
    }

    public String welcomeTemplate(String name, String password) {
        return "<h2>Welcome to " + PLATFORM_NAME + "!</h2>" +
                "<p>Your temporary password is: <strong>" + password + "</strong></p>" +
                "<p>Please log in and change it immediately.</p>";
    }

    public String generateInvoiceEmailBody(
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
            String paymentInstructions
    ) {
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 650px; margin: 0 auto; border: 1px solid #e0e0e0; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);\">");

        // Header / Logo
        if (managerLogoUrl != null && !managerLogoUrl.isEmpty()) {
            html.append("<div style=\"text-align: left; margin-bottom: 25px;\">");
            html.append("<img src=\"").append(managerLogoUrl).append("\" alt=\"").append(managerCompanyName).append("\" style=\"max-height: 60px; object-fit: contain;\">");
            html.append("</div>");
        }
        
        html.append("<h1 style=\"color: #1a202c; font-size: 24px; margin-bottom: 20px; border-bottom: 2px solid #edf2f7; padding-bottom: 15px;\">Invoice Notification</h1>");

        // Greeting
        html.append("<p style=\"font-size: 16px;\">Dear <strong>").append(clientName).append("</strong>,</p>");
        html.append("<p style=\"font-size: 15px; color: #4a5568;\">This is a courtesy notice regarding your recent invoice from <strong>").append(managerCompanyName).append("</strong>.</p>");

        // Summary Table
        html.append("<div style=\"background-color: #f7fafc; padding: 20px; border-radius: 6px; margin: 25px 0; border: 1px solid #e2e8f0;\">");
        html.append("<h2 style=\"margin-top: 0; color: #2d3748; font-size: 18px;\">Summary</h2>");
        html.append("<table style=\"width: 100%; border-collapse: collapse; font-size: 15px;\">");
        html.append("<tr><td style=\"padding: 10px 0; border-bottom: 1px solid #edf2f7; color: #718096;\">Invoice Number:</td><td style=\"padding: 10px 0; border-bottom: 1px solid #edf2f7; text-align: right; font-weight: bold;\">").append(invoiceNumber).append("</td></tr>");
        html.append("<tr><td style=\"padding: 10px 0; border-bottom: 1px solid #edf2f7; color: #718096;\">Total Amount:</td><td style=\"padding: 10px 0; border-bottom: 1px solid #edf2f7; text-align: right; font-weight: bold; color: #2c5282; font-size: 18px;\">").append(currencySymbol).append(" ").append(amount).append("</td></tr>");
        html.append("<tr><td style=\"padding: 10px 0; color: #718096;\">Due Date:</td><td style=\"padding: 10px 0; text-align: right; font-weight: bold; color: #e53e3e;\">").append(dueDate).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        // Payment Instructions
        if (paymentInstructions != null && !paymentInstructions.isEmpty()) {
            html.append("<h3 style=\"color: #2d3748; font-size: 16px; margin-bottom: 8px;\">Payment Instructions</h3>");
            html.append("<div style=\"background-color: #fffaf0; padding: 15px; border-left: 4px solid #ed8936; font-size: 14px; color: #744210; margin-bottom: 25px;\">").append(paymentInstructions).append("</div>");
        }

        // Closing
        html.append("<p style=\"font-size: 15px;\">Please find the detailed invoice attached to this email.</p>");
        html.append("<p style=\"font-size: 15px; margin-bottom: 30px;\">If you have any questions, feel free to contact us directly.</p>");

        // Signature
        html.append("<p style=\"margin-bottom: 5px; font-size: 15px;\">Best regards,</p>");
        html.append("<p style=\"font-weight: bold; font-size: 16px; color: #2d3748; margin-top: 0;\">").append(managerCompanyName).append("</p>");

        // Footer
        html.append("<div style=\"margin-top: 40px; border-top: 1px solid #edf2f7; padding-top: 20px; text-align: center; color: #a0aec0; font-size: 13px;\">");
        html.append("<p style=\"margin: 2px 0;\">").append(managerCompanyName).append("</p>");
        if (managerAddress != null && !managerAddress.isEmpty()) {
            html.append("<p style=\"margin: 2px 0;\">").append(managerAddress).append("</p>");
        }
        html.append("<p style=\"margin: 2px 0;\">Email: ").append(managerEmail).append(" | Phone: ").append(managerPhone).append("</p>");
        html.append("<p style=\"margin-top: 15px; color: #cbd5e0; font-style: italic;\">Powered by ").append(PLATFORM_NAME).append("</p>");
        html.append("</div>");

        html.append("</div>");
        return html.toString();
    }
}