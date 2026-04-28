package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class InvoiceCalculationService {

    /**
     * Recalculates the subtotal, tax, and total for the given invoice based on its line items.
     * This logic is extracted from the entity to follow SRP and OCP.
     *
     * @param invoice the invoice to recalculate
     */
    public void recalculateInvoice(Invoice invoice) {
        if (invoice == null || invoice.getLineItems() == null) {
            return;
        }

        BigDecimal subtotal = invoice.getLineItems().stream()
                .map(InvoiceLineItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(Invoice.TAX_RATE);
        BigDecimal total = subtotal.add(tax);

        invoice.setSubtotal(subtotal);
        invoice.setTax(tax);
        invoice.setTotal(total);
    }
}
