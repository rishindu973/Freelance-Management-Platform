package com.freelance.freelancepm.email;

public class SendGrid {

    private String apiKey;

    public SendGrid(String apiKey) {
        this.apiKey = apiKey;
    }

    public Response api(Request request) {
        // Simulate sending email
        System.out.println("Sending email using API Key: " + apiKey);
        System.out.println(request.getBody());

        // Simulate successful response
        return new Response(202); // HTTP 202 = Accepted
    }
}