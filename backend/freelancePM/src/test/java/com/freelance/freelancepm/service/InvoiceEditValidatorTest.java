package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.exception.InvoiceEditNotAllowedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceEditValidatorTest {

    private final InvoiceEditValidator validator = new InvoiceEditValidator();

    @ParameterizedTest
    @EnumSource(value = InvoiceStatus.class, names = {"DRAFT", "FINAL", "FAILED"})
    void validateEditable_AllowedStatuses_ShouldNotThrow(InvoiceStatus status) {
        Invoice invoice = new Invoice();
        invoice.setStatus(status);

        assertDoesNotThrow(() -> validator.validateEditable(invoice));
    }

    @ParameterizedTest
    @EnumSource(value = InvoiceStatus.class, names = {"SENT", "PAID", "OVERDUE", "PARTIALLY_PAID", "OVERPAID"})
    void validateEditable_DisallowedStatuses_ShouldThrowException(InvoiceStatus status) {
        Invoice invoice = new Invoice();
        invoice.setStatus(status);

        assertThrows(InvoiceEditNotAllowedException.class, () -> validator.validateEditable(invoice));
    }
}
