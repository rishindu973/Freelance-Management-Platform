package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProfitService {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalIncome() {
        return invoiceRepository.findByStatus(InvoiceStatus.PAID)
                .stream()
                .map(invoice -> invoice.getTotal() != null ? invoice.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
