package com.freelance.freelancepm.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Content;

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
        Content content = new Content("text/html", body);

        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sendGrid = new SendGrid(apiKey);

        int maxRetries = 3;

        for (int i = 1; i <= maxRetries; i++) {
            try {
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sendGrid.api(request);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    System.out.println("✅ Email sent to: " + to);
                    return;
                } else {
                    System.out.println("⚠ Attempt " + i + " failed. Status: " + response.getStatusCode());
                }

            } catch (Exception e) {
                System.out.println("❌ Error on attempt " + i + ": " + e.getMessage());
            }
        }

        System.out.println("🚨 Email failed after retries: " + to);
    }
}