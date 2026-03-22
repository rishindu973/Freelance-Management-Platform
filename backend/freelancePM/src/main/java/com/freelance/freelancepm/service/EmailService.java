package com.freelance.freelancepm.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

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