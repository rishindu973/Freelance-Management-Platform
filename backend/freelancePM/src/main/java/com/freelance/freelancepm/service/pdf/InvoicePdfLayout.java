package com.freelance.freelancepm.service.pdf;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.model.Client;

import java.io.IOException;
import java.util.List;

public interface InvoicePdfLayout {
    void drawHeader(PdfGenerationContext context) throws IOException;

    void drawInvoiceInfo(PdfGenerationContext context, Invoice invoice) throws IOException;

    void drawClientSection(PdfGenerationContext context, Client client) throws IOException;

    void drawTable(PdfGenerationContext context, List<InvoiceLineItem> lineItems) throws IOException;

    void drawTotalSection(PdfGenerationContext context, Invoice invoice) throws IOException;

    void drawFooter(PdfGenerationContext context) throws IOException;
}
