package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.PaymentDTO;
import com.freelance.freelancepm.entity.Payment;
import com.freelance.freelancepm.service.PaymentService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // SCRUM 46: Record client payment
    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO dto) {

        Payment payment = paymentService.recordPayment(dto.getInvoiceId(), dto.getAmount());

        PaymentDTO response = PaymentDTO.builder()
                .id(payment.getId())

                .invoiceId(payment.getInvoice().getId())

                .invoiceId(payment.getId())
                 (Add Updated PaymentController)
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<java.util.List<PaymentDTO>> getPaymentsByClient(@PathVariable Integer clientId) {
        java.util.List<Payment> payments = paymentService.getPaymentsByClient(clientId);
        java.util.List<PaymentDTO> dtos = payments.stream().map(payment -> PaymentDTO.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice().getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .build()).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping
    public ResponseEntity<java.util.List<PaymentDTO>> getAllPayments() {
        java.util.List<Payment> payments = paymentService.getAllPayments();
        java.util.List<PaymentDTO> dtos = payments.stream().map(payment -> PaymentDTO.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice().getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .build()).toList();
        return ResponseEntity.ok(dtos);
    }
}