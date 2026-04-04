package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, Long> {
    List<InvoiceLineItem> findByInvoiceId(Long invoiceId);
}
