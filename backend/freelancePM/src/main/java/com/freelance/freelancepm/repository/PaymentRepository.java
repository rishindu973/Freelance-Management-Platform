package com.freelance.freelancepm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freelance.freelancepm.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByInvoiceId(Integer invoiceId);

    List<Payment> findByInvoice_ClientId(Integer clientId);

    List<Payment> findByPaymentDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);
}
