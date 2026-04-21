package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
        List<Payment> findByInvoiceId(Integer invoiceId);

        List<Payment> findByInvoice_ClientId(Integer clientId);

        List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

        @Query("SELECT SUM(p.amount) FROM Payment p JOIN p.invoice.projects proj WHERE proj.id = :projectId " +
                        "AND p.paymentDate BETWEEN :start AND :end")
        BigDecimal sumIncomeByProject(@Param("projectId") Integer projectId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);
}
