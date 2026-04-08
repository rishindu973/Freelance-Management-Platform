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
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
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
    private final com.freelance.freelancepm.repository.ManagerRepository managerRepository;
    private final InvoicePdfLayout invoicePdfLayout;

    public byte[] generateInvoicePdf(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        List<InvoiceLineItem> lineItems = invoiceLineItemRepository.findByInvoiceId(invoiceId);

        // Fetch Manager Branding
        com.freelance.freelancepm.entity.Manager manager = null;
        if (invoice.getProject() != null && invoice.getProject().getManagerId() != null) {
            manager = managerRepository.findById(invoice.getProject().getManagerId()).orElse(null);
        }

        byte[] logoBytes = (manager != null) ? fetchLogoBytes(manager.getLogoUrl()) : null;

        try (PDDocument document = new PDDocument()) {
            PdfStyle style = (manager != null) ? PdfStyle.fromManager(manager) : PdfStyle.defaultStyle();
            
            try (PdfGenerationContext context = new PdfGenerationContext(document, style)) {
                invoicePdfLayout.drawHeader(context, invoice, manager, logoBytes);
                invoicePdfLayout.drawInvoiceInfo(context, invoice);
                invoicePdfLayout.drawClientSection(context, invoice, manager);
                invoicePdfLayout.drawTable(context, lineItems);
                invoicePdfLayout.drawTotalSection(context, invoice);
                invoicePdfLayout.drawFooter(context, manager);
            }

            // Post-processing: Add Page numbers on every page
            addPageNumbers(document, style);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            log.info("Successfully generated PDF for invoice ID: {}", invoiceId);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating PDF for invoice ID: {}", invoiceId, e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addPageNumbers(PDDocument document, PdfStyle style) throws IOException {
        int totalPages = document.getNumberOfPages();
        for (int i = 0; i < totalPages; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                // Position at bottom right
                float x = page.getMediaBox().getWidth() - style.getMargin() - 60;
                float y = style.getMargin() / 2;
                contentStream.newLineAtOffset(x, y);
                contentStream.showText("Page " + (i + 1) + " of " + totalPages);
                contentStream.endText();
            }
        }
    }

    private byte[] fetchLogoBytes(String logoUrl) {
        if (logoUrl == null || logoUrl.isEmpty()) return null;
        try {
            java.net.URL url = new java.net.URL(logoUrl);
            try (java.io.InputStream in = url.openStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch logo from URL: {}. Reason: {}", logoUrl, e.getMessage());
            return null;
        }
    }
}
