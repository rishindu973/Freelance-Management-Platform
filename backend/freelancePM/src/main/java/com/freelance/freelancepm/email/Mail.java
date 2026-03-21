package com.freelance.freelancepm.email;

public class Mail {

    private Email from;
    private String subject;
    private Email to;
    private EmailService.Content content;

    public Mail(Email from, String subject, Email to, EmailService.Content content) {
        this.from = from;
        this.subject = subject;
        this.to = to;
        this.content = content;
    }

    public String build() {
        // Return a simple string representation of the email
        return "From: " + from.getEmail() +
                "\nTo: " + to.getEmail() +
                "\nSubject: " + subject +
                "\nContent-Type: " + content.getType() +
                "\nBody: " + content.getValue();
    }
}