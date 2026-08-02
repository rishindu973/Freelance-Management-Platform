package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.entity.Payment;
import com.freelance.freelancepm.event.InvoicePaidEvent;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentValidationService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusTransitionService transitionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processPayment(Invoice invoice, Payment payment, String recordedBy) {
        payment.setInvoice(invoice);
        payment.setRecordedBy(recordedBy);
        paymentRepository.save(payment);

        checkIfFullyPaid(invoice, recordedBy);
    }

    @Transactional
    public void checkIfFullyPaid(Invoice invoice, String recordedBy) {
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return;
        }

        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(invoice.getTotal()) >= 0) {
            // Transition status
            transitionService.validateAndTransition(invoice, InvoiceStatus.PAID, recordedBy);
            
            invoice.setPaidTimestamp(LocalDateTime.now());
            invoiceRepository.save(invoice);

            // Trigger Event
            eventPublisher.publishEvent(new InvoicePaidEvent(
                    this,
                    invoice.getId(),
                    invoice.getClient().getId(),
                    totalPaid,
                    LocalDateTime.now()
            ));
        }
    }
}
