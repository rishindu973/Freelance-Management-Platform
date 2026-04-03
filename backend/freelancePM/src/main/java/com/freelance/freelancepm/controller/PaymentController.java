package com.freelance.freelancepm.controller;

public class Payment {
    @RestController
    @RequestMapping("/api/payments")
    @RequiredArgsConstructor
    public class PaymentController {

        private final PaymentService paymentService;

        @PostMapping
        public ResponseEntity<Payment> createPayment(
                @RequestParam Integer invoiceId,
                @RequestParam BigDecimal amount) {

            Payment payment = paymentService.recordPayment(invoiceId, amount);
            return ResponseEntity.ok(payment);
        }
    }
}
