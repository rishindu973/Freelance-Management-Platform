package com.freelance.freelancepm.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {

        Email from = new Email(fromEmail);
        Email recipient = new Email(to);
        Mail mail = new Mail(from, subject, recipient, new Content("text/html", body));

        SendGrid sendGrid = new SendGrid(apiKey);

        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Request request = new Request();
                request.setMethod("POST");
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sendGrid.api(request);

                if (response.getStatus() >= 200 && response.getStatus() < 300) {
                    System.out.println("Email sent successfully to: " + to);
                    return;
                } else {
                    System.out.println("Failed attempt " + attempt + " | Status: " + response.getStatus());
                }
            } catch (Exception e) {
                System.out.println("Error on attempt " + attempt + ": " + e.getMessage());
            }
        }

        System.out.println("Email failed after " + maxRetries + " attempts for: " + to);
    }

    // Inner class to replace AbstractDocument.Content
    public static class Content {
        private String type;
        private String value;

        public Content(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }
}