package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.exception.InvoiceEditNotAllowedException;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEditValidator {

    public void validateEditable(Invoice invoice) {
        if (invoice == null || invoice.getStatus() == null) {
            return;
        }

        InvoiceStatus status = invoice.getStatus();
        
        switch (status) {
            case SENT:
            case PAID:
            case OVERDUE:
            case PARTIALLY_PAID:
            case OVERPAID:
                throw new InvoiceEditNotAllowedException("Cannot edit sent invoice. Please create a credit note.");
            case DRAFT:
            case FINAL:
            case FAILED:
                // Editing is allowed
                break;
            default:
                throw new InvoiceEditNotAllowedException("Editing not allowed for status: " + status);
        }
    }
}
