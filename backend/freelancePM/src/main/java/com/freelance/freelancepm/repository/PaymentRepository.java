package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByInvoiceId(Integer invoiceId);
    List<Payment> findByInvoice_ClientId(Integer clientId);
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);
}
