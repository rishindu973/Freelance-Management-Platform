package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.repository.InvoiceLineItemRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.service.pdf.InvoicePdfLayout;
import com.freelance.freelancepm.service.pdf.PdfGenerationContext;
import com.freelance.freelancepm.service.pdf.PdfStyle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final InvoicePdfLayout invoicePdfLayout;

    public byte[] generateInvoicePdf(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        List<InvoiceLineItem> lineItems = invoiceLineItemRepository.findByInvoiceId(invoiceId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PdfStyle style = PdfStyle.defaultStyle();
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PdfGenerationContext context = new PdfGenerationContext(document, contentStream, style);

                invoicePdfLayout.drawHeader(context);
                invoicePdfLayout.drawInvoiceInfo(context, invoice);
                invoicePdfLayout.drawClientSection(context, invoice.getClient());
                invoicePdfLayout.drawTable(context, lineItems);
                invoicePdfLayout.drawTotalSection(context, invoice);
                invoicePdfLayout.drawFooter(context);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            log.info("Successfully generated PDF for invoice ID: {}", invoiceId);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating PDF for invoice ID: {}", invoiceId, e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
