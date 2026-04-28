package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer>, JpaSpecificationExecutor<Invoice> {
    @EntityGraph(attributePaths = {"client"})
    Page<Invoice> findAll(@Nullable Specification<Invoice> spec, Pageable pageable);

    List<Invoice> findByStatus(InvoiceStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
