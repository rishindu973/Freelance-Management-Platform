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
        org.apache.pdfbox.pdmodel.font.PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        org.apache.pdfbox.pdmodel.font.PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        java.awt.Color color = java.awt.Color.BLACK;

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float rightEdge = width - margin;
        float y = context.getYPosition();

        // Column X coordinates
        float colItemX = margin;
        float colDescX = margin + 40;
        float colQtyX = rightEdge - 180;
        float colPriceX = rightEdge - 90;
        float colAmountX = rightEdge;

        // Header Row
        context.drawLine(margin, y + 10, rightEdge, y + 10, color, 1f);
        context.drawText("Item", colItemX, y, fontBold, 10, color);
        context.drawText("Description", colDescX, y, fontBold, 10, color);
        context.drawRightAlignedText("Quantity", colQtyX, y, fontBold, 10, color);
        context.drawRightAlignedText("Unit Price", colPriceX, y, fontBold, 10, color);
        context.drawRightAlignedText("Amount", colAmountX, y, fontBold, 10, color);
        y -= 15;
        context.drawLine(margin, y + 10, rightEdge, y + 10, color, 1f);

        // Data Rows
        y -= 15;
        if (lineItems != null) {
            int itemIndex = 1;
            for (InvoiceLineItem item : lineItems) {
                context.setYPosition(y);
                context.ensureSpace(30, (ctx) -> {
                    float headerY = ctx.getYPosition();
                    ctx.drawLine(margin, headerY + 10, rightEdge, headerY + 10, color, 1f);
                    ctx.drawText("Item", colItemX, headerY, fontBold, 10, color);
                    ctx.drawText("Description", colDescX, headerY, fontBold, 10, color);
                    ctx.drawRightAlignedText("Quantity", colQtyX, headerY, fontBold, 10, color);
                    ctx.drawRightAlignedText("Unit Price", colPriceX, headerY, fontBold, 10, color);
                    ctx.drawRightAlignedText("Amount", colAmountX, headerY, fontBold, 10, color);
                    ctx.setYPosition(headerY - 15);
                    ctx.drawLine(margin, ctx.getYPosition() + 10, rightEdge, ctx.getYPosition() + 10, color, 1f);
                    ctx.setYPosition(ctx.getYPosition() - 15);
                });
                
                y = context.getYPosition();

                java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                String qtyStr = item.getQuantity() != null ? item.getQuantity().toString() : "0";
                String priceStr = item.getUnitPrice() != null ? "$" + df.format(item.getUnitPrice()) : "$0.00";
                String amountStr = item.getAmount() != null ? "$" + df.format(item.getAmount()) : "$0.00";
                String desc = item.getDescription() != null ? item.getDescription() : "";

                context.drawText(String.valueOf(itemIndex++), colItemX, y, fontRegular, 10, color);

                List<String> lines = context.parseLines(desc, colQtyX - colDescX - 20, fontRegular, 10);
                float descY = y;
                for (String line : lines) {
                    context.drawText(line, colDescX, descY, fontRegular, 10, color);
                    descY -= 12;
                }

                context.drawRightAlignedText(qtyStr, colQtyX, y, fontRegular, 10, color);
                context.drawRightAlignedText(priceStr, colPriceX, y, fontRegular, 10, color);
                context.drawRightAlignedText(amountStr, colAmountX, y, fontRegular, 10, color);

                float rowHeight = Math.max(20, lines.size() * 12 + 10);
                y -= rowHeight;
            }
        }

        context.drawLine(margin, y + 10, rightEdge, y + 10, color, 1f);
        context.setYPosition(y - 20);
    }

    protected void drawTotalsSection(PdfGenerationContext context, Invoice invoice) throws IOException {
        context.ensureSpace(120, null); // Maintain spacing to avoid overlapping the footer
        
        org.apache.pdfbox.pdmodel.font.PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        org.apache.pdfbox.pdmodel.font.PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        java.awt.Color color = java.awt.Color.BLACK;

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float rightEdge = width - margin;
        float y = context.getYPosition();

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
        String subtotalStr = invoice.getSubtotal() != null ? "$" + df.format(invoice.getSubtotal()) : "$0.00";
        String totalStr = invoice.getTotal() != null ? "$" + df.format(invoice.getTotal()) : "$0.00";

        // LEFT: Notes
        float leftY = y;
        context.drawText("NOTES:", margin, leftY, fontBold, 10, color);
        leftY -= 15;
        String notes = invoice.getDescription();
        if (notes != null && !notes.isEmpty()) {
            List<String> lines = context.parseLines(notes, 250, fontRegular, 10);
            for (String line : lines) {
                context.drawText(line, margin, leftY, fontRegular, 10, color);
                leftY -= 12;
            }
        } else {
            context.drawText("Thank you for your business.", margin, leftY, fontRegular, 10, color);
            leftY -= 12;
        }

        // RIGHT: Subtotal / Total
        float rightY = y;
        float labelX = rightEdge - 150;

        // Subtotal
        context.drawText("Subtotal", labelX, rightY, fontRegular, 10, color);
        context.drawRightAlignedText(subtotalStr, rightEdge, rightY, fontRegular, 10, color);
        rightY -= 20;

        // Total
        context.drawText("TOTAL", labelX, rightY, fontBold, 14, color);
        context.drawRightAlignedText(totalStr, rightEdge, rightY, fontBold, 14, color);
        rightY -= 20;

        context.setYPosition(Math.min(leftY, rightY) - 30);
    }

    protected void drawFooter(PdfGenerationContext context, Manager manager) throws IOException {
        org.apache.pdfbox.pdmodel.font.PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        org.apache.pdfbox.pdmodel.font.PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        java.awt.Color color = java.awt.Color.BLACK;

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float centerX = width / 2;
        float footerY = 60; // Fixed footer position at the bottom of the page

        // Horizontal Line above footer
        context.drawLine(margin, footerY + 20, width - margin, footerY + 20, color, 0.5f);

        // Center: "Powered by FREELANCEFLOW" and Placeholder Logo
        String text1 = "Powered by FREELANCEFLOW";
        float text1Width = fontBold.getStringWidth(text1) / 1000 * 10;

        float logoBoxSize = 12;
        float gap = 5;
        float totalTopWidth = logoBoxSize + gap + text1Width;
        float startTopX = centerX - (totalTopWidth / 2);

        // Placeholder Logo (a simple unfilled box + cross)
        context.drawRect(startTopX, footerY - 2, logoBoxSize, logoBoxSize, color, false);
        context.drawLine(startTopX, footerY - 2, startTopX + logoBoxSize, footerY - 2 + logoBoxSize, color, 0.5f);
        context.drawLine(startTopX, footerY - 2 + logoBoxSize, startTopX + logoBoxSize, footerY - 2, color, 0.5f);

        // "Powered by FREELANCEFLOW"
        context.drawText(text1, startTopX + logoBoxSize + gap, footerY, fontBold, 10, color);

        // Below text
        String text2Part1 = "This invoice was generated using FREELANCEFLOW. Visit ";
        String linkText = "https://freelanceflow.com";
        String text2Part3 = " for more information.";

        float text2Part1Width = fontRegular.getStringWidth(text2Part1) / 1000 * 8;
        float linkTextWidth = fontRegular.getStringWidth(linkText) / 1000 * 8;
        float text2Part3Width = fontRegular.getStringWidth(text2Part3) / 1000 * 8;

        float totalWidth = text2Part1Width + linkTextWidth + text2Part3Width;
        float startX = centerX - (totalWidth / 2);
        float linkStartX = startX + text2Part1Width;
        float linkY = footerY - 15;

        context.drawText(text2Part1, startX, linkY, fontRegular, 8, color);

        // Link text (underlined)
        context.drawText(linkText, linkStartX, linkY, fontRegular, 8, color);
        context.drawLine(linkStartX, linkY - 1, linkStartX + linkTextWidth, linkY - 1, color, 0.5f);

        // Remaining text
        context.drawText(text2Part3, linkStartX + linkTextWidth, linkY, fontRegular, 8, color);

        // Add PDF Hyperlink Annotation (Ensure clickable PDF link)
        org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink txtLink = new org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink();
        org.apache.pdfbox.pdmodel.interactive.action.PDActionURI action = new org.apache.pdfbox.pdmodel.interactive.action.PDActionURI();
        action.setURI(linkText);
        txtLink.setAction(action);

        org.apache.pdfbox.pdmodel.common.PDRectangle position = new org.apache.pdfbox.pdmodel.common.PDRectangle();
        position.setLowerLeftX(linkStartX);
        position.setLowerLeftY(linkY - 2);
        position.setUpperRightX(linkStartX + linkTextWidth);
        position.setUpperRightY(linkY + 10);
        txtLink.setRectangle(position);

        org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary borderULine = new org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary();
        borderULine.setStyle(org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary.STYLE_UNDERLINE);
        borderULine.setWidth(0); // hide native border since we drew an underline manually
        txtLink.setBorderStyle(borderULine);

        org.apache.pdfbox.pdmodel.PDDocument document = context.getDocument();
        org.apache.pdfbox.pdmodel.PDPage page = document.getPage(context.getCurrentPageNumber() - 1);
        page.getAnnotations().add(txtLink);
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
