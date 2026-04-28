package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findByInvoiceId(Integer invoiceId);
}
