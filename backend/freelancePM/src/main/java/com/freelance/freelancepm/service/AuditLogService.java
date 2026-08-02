package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.InvoiceStatusAuditLog;
import com.freelance.freelancepm.repository.InvoiceStatusAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final InvoiceStatusAuditLogRepository auditLogRepository;

    public void logStatusChange(Integer invoiceId, 
                                com.freelance.freelancepm.entity.InvoiceStatus oldStatus, 
                                com.freelance.freelancepm.entity.InvoiceStatus newStatus, 
                                String changedBy) {
        InvoiceStatusAuditLog log = InvoiceStatusAuditLog.builder()
                .invoiceId(invoiceId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .build();
        auditLogRepository.save(log);
    }
}
