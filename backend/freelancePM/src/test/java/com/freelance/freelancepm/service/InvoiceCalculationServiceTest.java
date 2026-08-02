package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceCalculationServiceTest {

    private final InvoiceCalculationService calculationService = new InvoiceCalculationService();

    @Test
    void recalculateInvoice_ShouldComputeSubtotalTaxAndTotal() {
        // Arrange
        Invoice invoice = new Invoice();
        InvoiceLineItem item1 = new InvoiceLineItem();
        item1.setAmount(new BigDecimal("100.00")); // quantity * price
        
        InvoiceLineItem item2 = new InvoiceLineItem();
        item2.setAmount(new BigDecimal("50.50"));

        invoice.setLineItems(List.of(item1, item2));

        // Act
        calculationService.recalculateInvoice(invoice);

        // Assert
        // Expected subtotal: 100.00 + 50.50 = 150.50
        // Expected tax: 150.50 * 0.10 = 15.05
        // Expected total: 150.50 + 15.05 = 165.55
        assertEquals(0, new BigDecimal("150.50").compareTo(invoice.getSubtotal()));
        assertEquals(0, new BigDecimal("15.05").compareTo(invoice.getTax()));
        assertEquals(0, new BigDecimal("165.55").compareTo(invoice.getTotal()));
    }

    @Test
    void recalculateInvoice_EmptyItems_ShouldSetZeros() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setLineItems(List.of());

        // Act
        calculationService.recalculateInvoice(invoice);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.getSubtotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.getTax()));
        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.getTotal()));
    }
}
