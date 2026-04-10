package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.InvoiceStatusAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceStatusAuditLogRepository extends JpaRepository<InvoiceStatusAuditLog, Long> {
    List<InvoiceStatusAuditLog> findByInvoiceIdOrderByLoggedAtDesc(Integer invoiceId);
}
