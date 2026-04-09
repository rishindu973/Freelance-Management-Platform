package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.Payment;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    // SCRUM 46: Track client payments (completed and due)
    public Payment recordPayment(Integer invoiceId, BigDecimal amount) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Create payment
        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(amount)
                .paymentDate(LocalDate.now())
                .status("completed")
                .build();

        paymentRepository.save(payment);

        // Calculate total paid
        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);

        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal invoiceAmount = invoice.getTotal();

        // Remaining balance
        BigDecimal remaining = invoiceAmount.subtract(totalPaid);

        // SCRUM 46: Partial / Full / Overpayment handling
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(Invoice.Status.PAID);
        } else if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(Invoice.Status.PARTIALLY_PAID);
        } else {
            // SCRUM 46: Overpayment handling
            invoice.setStatus(Invoice.Status.OVERPAID);
        }

        invoiceRepository.save(invoice);

        return payment;
    }

    // SCRUM 46: Get payment history
    public List<Payment> getPaymentsByInvoice(Integer invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    // SCRUM 46: Calculate outstanding balance
    public BigDecimal getRemainingBalance(Integer invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow();

        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);

        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return invoice.getTotal().subtract(totalPaid);
    }

    public List<Payment> getPaymentsByClient(Integer clientId) {
        return paymentRepository.findByInvoice_ClientId(clientId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}