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
        org.apache.pdfbox.pdmodel.font.PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        org.apache.pdfbox.pdmodel.font.PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        java.awt.Color color = java.awt.Color.BLACK;

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float y = context.getYPosition();

        // LEFT: "INVOICE" (large text)
        context.drawText("INVOICE", margin, y - 30, fontBold, 36, color);

        // RIGHT: Company Details
        float rightX = width - margin;
        float rightY = y;

        // Logo (top right)
        if (logoBytes != null) {
            context.drawImage(logoBytes, rightX - 60, rightY - 40, 60, 40);
            rightY -= 50;
        }

        // Company Name
        String companyName = (manager != null && manager.getCompanyName() != null && !manager.getCompanyName().isEmpty()) 
                                ? manager.getCompanyName() : "Company Name";
        context.drawRightAlignedText(companyName, rightX, rightY, fontBold, 12, color);
        rightY -= 15;

        // Owner Name
        String ownerName = (manager != null && manager.getFullName() != null && !manager.getFullName().isEmpty()) 
                                ? manager.getFullName() : "Owner Name";
        context.drawRightAlignedText(ownerName, rightX, rightY, fontRegular, 10, color);
        rightY -= 15;

        // Address
        String address = (manager != null && manager.getAddress() != null && !manager.getAddress().isEmpty()) 
                                ? manager.getAddress() : "Company Address";
        context.drawRightAlignedText(address, rightX, rightY, fontRegular, 10, color);
        rightY -= 15;

        // Phone
        String phone = (manager != null && manager.getContactNumber() != null && !manager.getContactNumber().isEmpty()) 
                                ? manager.getContactNumber() : "Company Phone";
        context.drawRightAlignedText(phone, rightX, rightY, fontRegular, 10, color);
        rightY -= 15;

        // Email
        String email = (manager != null && manager.getUser() != null && manager.getUser().getEmail() != null) 
                                ? manager.getUser().getEmail() : "company@email.com";
        context.drawRightAlignedText(email, rightX, rightY, fontRegular, 10, color);

        // Set Y position below the lowest component in the header
        context.setYPosition(Math.min(y - 50, rightY - 30));
    }

    protected void drawBillingSection(PdfGenerationContext context, Invoice invoice, Manager manager) throws IOException {
        org.apache.pdfbox.pdmodel.font.PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        org.apache.pdfbox.pdmodel.font.PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        java.awt.Color color = java.awt.Color.BLACK;

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float y = context.getYPosition();

        // LEFT: BILL TO
        float leftY = y;
        context.drawText("BILL TO", margin, leftY, fontBold, 12, color);
        leftY -= 15;

        com.freelance.freelancepm.model.Client client = invoice.getClient();
        if (client != null) {
            String clientName = (client.getName() != null && !client.getName().isEmpty()) ? client.getName() : "Client Name";
            context.drawText(clientName, margin, leftY, fontBold, 10, color);
            leftY -= 15;

            String address = (client.getAddress() != null && !client.getAddress().isEmpty()) ? client.getAddress() : "Client Address";
            context.drawText(address, margin, leftY, fontRegular, 10, color);
            leftY -= 15;

            String phone = (client.getPhone() != null && !client.getPhone().isEmpty()) ? client.getPhone() : "Client Phone";
            context.drawText(phone, margin, leftY, fontRegular, 10, color);
            leftY -= 15;

            String email = (client.getEmail() != null && !client.getEmail().isEmpty()) ? client.getEmail() : "client@email.com";
            context.drawText(email, margin, leftY, fontRegular, 10, color);
            leftY -= 15;
        } else {
            leftY -= 60; // Leave space anyway if client is null
        }

        // RIGHT: Invoice Info
        float rightX = width - margin;
        float rightY = y;

        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");

        // Invoice Number
        context.drawRightAlignedText("Invoice Number: " + invoice.getInvoiceNumber(), rightX, rightY, fontRegular, 10, color);
        rightY -= 15;

        // Issue Date
        String issueDateStr = invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(dateFormatter) : "N/A";
        context.drawRightAlignedText("Issue Date: " + issueDateStr, rightX, rightY, fontRegular, 10, color);
        rightY -= 15;

        // Due Date
        String dueDateStr = invoice.getDueDate() != null ? invoice.getDueDate().format(dateFormatter) : "N/A";
        context.drawRightAlignedText("Due Date: " + dueDateStr, rightX, rightY, fontRegular, 10, color);
        rightY -= 15;

        context.setYPosition(Math.min(leftY, rightY) - 30);
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
