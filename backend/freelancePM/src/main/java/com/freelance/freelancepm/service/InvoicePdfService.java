package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.repository.InvoiceLineItemRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.repository.ManagerRepository;
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

/**
 * Orchestrates invoice PDF generation.
 *
 * <p>This service is responsible for assembling data and delegating
 * rendering to the {@link InvoicePdfLayout} strategy. It does not
 * contain any drawing logic itself — that lives entirely in the
 * {@code service.pdf} package.</p>
 *
 * <h3>Two entry points:</h3>
 * <ul>
 *   <li>{@link #generateInvoicePdf(Invoice)} — Core method. Accepts a
 *       fully-loaded Invoice entity (with line items initialized).
 *       Ideal for callers that already hold the entity (e.g. after
 *       create/update) or for unit testing without repository mocks.</li>
 *   <li>{@link #generateInvoicePdf(Integer)} — Convenience overload.
 *       Fetches the Invoice + line items from the database, then
 *       delegates to the core method.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final ManagerRepository managerRepository;
    private final InvoicePdfLayout invoicePdfLayout;

    // ──────────────────────────────────────────────
    //  Core method — accepts a loaded Invoice entity
    // ──────────────────────────────────────────────

    /**
     * Generates an invoice PDF from a fully-loaded {@link Invoice} entity.
     *
     * <p>The invoice's {@code lineItems} collection must be initialized
     * (non-lazy) before calling this method. Manager branding data is
     * resolved internally from the invoice's project association.</p>
     *
     * @param invoice the invoice entity with line items loaded
     * @return PDF document as a byte array
     * @throws IllegalArgumentException if invoice is null
     * @throws RuntimeException         if PDF generation fails
     */
    public byte[] generateInvoicePdf(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice must not be null");
        }

        List<InvoiceLineItem> lineItems = invoice.getLineItems();
        if (lineItems == null || lineItems.isEmpty()) {
            log.warn("Invoice ID {} has no line items — PDF will contain an empty table", invoice.getId());
        }

        // Resolve manager branding from the invoice's project
        Manager manager = resolveManager(invoice);
        byte[] logoBytes = fetchLogoBytes(manager);

        // Determine styling: use manager branding or fall back to defaults
        PdfStyle style = (manager != null) ? PdfStyle.fromManager(manager) : PdfStyle.defaultStyle();

        try (PDDocument document = new PDDocument()) {
            try (PdfGenerationContext context = new PdfGenerationContext(document, style)) {
                invoicePdfLayout.drawHeader(context, invoice, manager, logoBytes);
                invoicePdfLayout.drawInvoiceInfo(context, invoice);
                invoicePdfLayout.drawClientSection(context, invoice, manager);
                invoicePdfLayout.drawTable(context, lineItems != null ? lineItems : List.of());
                invoicePdfLayout.drawTotalSection(context, invoice);
                invoicePdfLayout.drawFooter(context, manager);
            }

            // Post-processing: add page numbers on every page
            addPageNumbers(document, style);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            log.info("Successfully generated PDF for invoice ID: {}", invoice.getId());
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating PDF for invoice ID: {}", invoice.getId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    // ──────────────────────────────────────────────────────
    //  Convenience overload — fetches data, then delegates
    // ──────────────────────────────────────────────────────

    /**
     * Fetches the invoice by ID and generates a PDF.
     *
     * <p>This is a convenience wrapper around {@link #generateInvoicePdf(Invoice)}.
     * It loads the invoice from the database, eagerly fetches line items
     * (to avoid Hibernate lazy-loading issues outside a transaction),
     * and delegates to the core method.</p>
     *
     * @param invoiceId the database ID of the invoice
     * @return PDF document as a byte array
     * @throws IllegalArgumentException if the invoice is not found
     */
    public byte[] generateInvoicePdf(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        // Eagerly load line items to avoid lazy-init exceptions
        // when the core method accesses them outside this transaction boundary
        List<InvoiceLineItem> lineItems = invoiceLineItemRepository.findByInvoiceId(invoiceId);
        invoice.getLineItems().clear();
        invoice.getLineItems().addAll(lineItems);

        return generateInvoicePdf(invoice);
    }

    // ──────────────────────────────────────────────
    //  Private helpers
    // ──────────────────────────────────────────────

    /**
     * Resolves the {@link Manager} associated with this invoice's project.
     * Returns {@code null} if no project or manager is linked.
     */
    private Manager resolveManager(Invoice invoice) {
        if (invoice.getProject() != null && invoice.getProject().getManagerId() != null) {
            return managerRepository.findById(invoice.getProject().getManagerId()).orElse(null);
        }
        return null;
    }

    /**
     * Fetches the manager's logo as a byte array from its remote URL.
     * Returns {@code null} gracefully on any failure.
     */
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

    /**
     * Post-processes the completed document to stamp page numbers
     * ("Page X of Y") on the bottom-right of every page.
     */
    private void addPageNumbers(PDDocument document, PdfStyle style) throws IOException {
        int totalPages = document.getNumberOfPages();
        for (int i = 0; i < totalPages; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
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
}
