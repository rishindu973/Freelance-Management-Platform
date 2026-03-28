package com.freelance.freelancepm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class WebhookController {

    /**
     * Consumes standard asynchronous JSON event payloads from SendGrid POST
     * webhooks.
     * Logs physical delivery status, bounces, and inbox open activity to fulfill
     * tracking mechanisms.
     */
    @PostMapping("/sendgrid")
    public ResponseEntity<String> handleSendGridEvents(@RequestBody List<Map<String, Object>> events) {
        if (events == null || events.isEmpty()) {
            return ResponseEntity.ok("No events received.");
        }

        // Standard logging pipeline for analytics
        for (Map<String, Object> event : events) {
            String eventType = (String) event.get("event");
            String email = (String) event.get("email");
            System.out.println(
                    "SendGrid Analytics Webhook -> [" + eventType.toUpperCase() + "] triggered for target: " + email);
        }

        return ResponseEntity.ok("Events processed successfully");
    }
}
