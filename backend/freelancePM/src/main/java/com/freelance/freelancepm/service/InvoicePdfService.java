package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.repository.InvoiceLineItemRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.repository.ManagerRepository;
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

/**
 * Orchestrates invoice PDF generation.
 *
 * <p>This service provides the base structure for a new invoice PDF template.
 * It uses modular skeleton methods for rendering different sections of the document.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final ManagerRepository managerRepository;

    public byte[] generateInvoicePdf(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice must not be null");
        }

        List<InvoiceLineItem> lineItems = invoice.getLineItems();
        if (lineItems == null || lineItems.isEmpty()) {
            log.warn("Invoice ID {} has no line items — PDF will contain an empty table", invoice.getId());
        }

        Manager manager = resolveManager(invoice);
        byte[] logoBytes = fetchLogoBytes(manager);

        PdfStyle style = (manager != null) ? PdfStyle.fromManager(manager) : PdfStyle.defaultStyle();

        try (PDDocument document = new PDDocument()) {
            try (PdfGenerationContext context = new PdfGenerationContext(document, style)) {
                // PDF Layout construction sequence (Modular methods)
                drawHeader(context, invoice, manager, logoBytes);
                drawBillingSection(context, invoice, manager);
                drawTable(context, lineItems != null ? lineItems : List.of());
                drawTotalsSection(context, invoice);
                drawFooter(context, manager);
            }

            // Post-processing: add page numbers on every page
            addPageNumbers(document, style);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            log.info("Successfully generated PDF skeleton for invoice ID: {}", invoice.getId());
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating PDF for invoice ID: {}", invoice.getId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public byte[] generateInvoicePdf(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        List<InvoiceLineItem> lineItems = invoiceLineItemRepository.findByInvoiceId(invoiceId);
        invoice.getLineItems().clear();
        invoice.getLineItems().addAll(lineItems);

        return generateInvoicePdf(invoice);
    }

    // ──────────────────────────────────────────────
    //  Template Methods for PDF Layout Skeletons
    // ──────────────────────────────────────────────

    protected void drawHeader(PdfGenerationContext context, Invoice invoice, Manager manager, byte[] logoBytes) throws IOException {
        // TODO: Implement header layout (e.g., Logo, Company Details)
        log.debug("Drawing header skeleton...");
    }

    protected void drawBillingSection(PdfGenerationContext context, Invoice invoice, Manager manager) throws IOException {
        // TODO: Implement billing section layout (e.g., Bill To, Status, Dates)
        log.debug("Drawing billing section skeleton...");
    }

    protected void drawTable(PdfGenerationContext context, List<InvoiceLineItem> lineItems) throws IOException {
        // TODO: Implement invoice items table
        log.debug("Drawing table skeleton...");
    }

    protected void drawTotalsSection(PdfGenerationContext context, Invoice invoice) throws IOException {
        // TODO: Implement totals section (e.g., Subtotal, Tax, Final Amount)
        log.debug("Drawing totals section skeleton...");
    }

    protected void drawFooter(PdfGenerationContext context, Manager manager) throws IOException {
        // TODO: Implement footer terms and conditions
        log.debug("Drawing footer skeleton...");
    }

    // ──────────────────────────────────────────────
    //  Private helpers
    // ──────────────────────────────────────────────

    private Manager resolveManager(Invoice invoice) {
        if (invoice.getProject() != null && invoice.getProject().getManagerId() != null) {
            return managerRepository.findById(invoice.getProject().getManagerId()).orElse(null);
        }
        return null;
    }

    private byte[] fetchLogoBytes(Manager manager) {
        if (manager == null) return null;
        String logoUrl = manager.getLogoUrl();
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

    private void addPageNumbers(PDDocument document, PdfStyle style) throws IOException {
        int totalPages = document.getNumberOfPages();
        for (int i = 0; i < totalPages; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                float x = page.getMediaBox().getWidth() - style.getMargin() - 60;
                float y = style.getMargin() / 2;
                contentStream.newLineAtOffset(x, y);
                contentStream.showText("Page " + (i + 1) + " of " + totalPages);
                contentStream.endText();
            }
        }
    }
}
