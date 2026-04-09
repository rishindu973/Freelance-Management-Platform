package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Composable JPA Specifications for the Invoice entity.
 * Each static method returns a single, reusable predicate that can be
 * combined with {@code Specification.where(...).and(...)} to build
 * arbitrary filter combinations without if-else chains (OCP-compliant).
 */
public class InvoiceSpecifications {

    private InvoiceSpecifications() {
        // utility class — prevent instantiation
    }

    /**
     * Filters invoices by the owning client's ID.
     */
    public static Specification<Invoice> clientIdEquals(Integer clientId) {
        return (root, query, cb) -> cb.equal(root.get("client").get("id"), clientId);
    }

    /**
     * Filters invoices created on or after the given date-time.
     */
    public static Specification<Invoice> createdOnOrAfter(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    /**
     * Filters invoices created on or before the given date-time.
     */
    public static Specification<Invoice> createdOnOrBefore(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
