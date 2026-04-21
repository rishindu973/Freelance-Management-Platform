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
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Orchestrates invoice PDF generation using the warm yellow palette.
 *
 * <p>
 * Layout mirrors the frontend InvoicePreview component exactly so that
 * the on-screen preview and the downloaded file look identical.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final ManagerRepository managerRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public byte[] generateInvoicePdf(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice must not be null");
        }

        log.info("[PDF] Starting generation for invoice ID: {}, number: {}",
                invoice.getId(), invoice.getInvoiceNumber());

        List<InvoiceLineItem> lineItems = invoice.getLineItems();
        if (lineItems == null || lineItems.isEmpty()) {
            log.warn("[PDF] Invoice ID {} has no line items — PDF will contain an empty table",
                    invoice.getId());
        } else {
            log.debug("[PDF] Invoice ID {} has {} line item(s)", invoice.getId(), lineItems.size());
        }

        log.debug("[PDF] Resolving manager for invoice ID: {}", invoice.getId());
        Manager manager = resolveManager(invoice);
        if (manager == null) {
            log.warn("[PDF] No manager found for invoice ID: {} — using fallback branding",
                    invoice.getId());
        } else {
            log.debug("[PDF] Manager resolved: {} (company: {})",
                    manager.getFullName(), manager.getCompanyName());
        }

        byte[] logoBytes = fetchLogoBytes(manager);
        PdfStyle style = (manager != null) ? PdfStyle.fromManager(manager) : PdfStyle.defaultStyle();

        try (PDDocument document = new PDDocument()) {
            try (PdfGenerationContext context = new PdfGenerationContext(document, style)) {

                log.debug("[PDF] Drawing header for invoice ID: {}", invoice.getId());
                drawHeader(context, invoice, manager, logoBytes);

                log.debug("[PDF] Drawing billing section for invoice ID: {}", invoice.getId());
                drawBillingSection(context, invoice, manager);

                log.debug("[PDF] Drawing line-items table ({} items) for invoice ID: {}",
                        lineItems != null ? lineItems.size() : 0, invoice.getId());
                drawTable(context, lineItems != null ? lineItems : List.of());

                log.debug("[PDF] Drawing totals section for invoice ID: {}", invoice.getId());
                drawTotalsSection(context, invoice);

                log.debug("[PDF] Drawing footer for invoice ID: {}", invoice.getId());
                drawFooter(context, manager);
            }

            addPageNumbers(document, style);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            log.info("[PDF] Successfully generated {} bytes for invoice ID: {}",
                    baos.size(), invoice.getId());
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("[PDF] IOException during generation for invoice ID: {} — {}",
                    invoice.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF for invoice " + invoice.getInvoiceNumber(), e);
        } catch (Exception e) {
            log.error("[PDF] Unexpected error during generation for invoice ID: {} — {}",
                    invoice.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF for invoice " + invoice.getInvoiceNumber(), e);
        }
    }

    /**
     * Loads an invoice by ID and generates its PDF.
     *
     * <p>
     * MUST run inside a transaction so that lazy associations
     * ({@code invoice.lineItems}, {@code invoice.project}) can be
     * initialized without a {@code LazyInitializationException}.
     * </p>
     */
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Integer invoiceId) {
        log.info("[PDF] Lookup triggered for invoice ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> {
                    log.error("[PDF] Invoice not found in database: ID={}", invoiceId);
                    return new IllegalArgumentException("Invoice not found with ID: " + invoiceId);
                });

        log.debug("[PDF] Invoice found: number={}, status={}, client={}",
                invoice.getInvoiceNumber(),
                invoice.getStatus(),
                invoice.getClient() != null ? invoice.getClient().getName() : "<null>");

        // Explicitly load line items — forces initialization while the session is still
        // open
        List<InvoiceLineItem> lineItems = invoiceLineItemRepository.findByInvoiceId(invoiceId);
        log.debug("[PDF] Explicitly loaded {} line item(s) for invoice ID: {}",
                lineItems.size(), invoiceId);

        // Replace the lazy collection with the eagerly-loaded list
        invoice.getLineItems().clear();
        invoice.getLineItems().addAll(lineItems);

        // Explicitly initialize the projects collection
        if (invoice.getProjects() != null) {
            invoice.getProjects().size();
        }

        return generateInvoicePdf(invoice);
    }

    // ──────────────────────────────────────────────
    // Section Renderers
    // ──────────────────────────────────────────────

    /**
     * Header: yellow background bar.
     * Left — "INVOICE" title + Issue Date + Due Date.
     * Right — "Powered Via / FREELANCEFLOW / email".
     */
    protected void drawHeader(PdfGenerationContext context, Invoice invoice, Manager manager, byte[] logoBytes)
            throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        org.apache.pdfbox.pdmodel.font.PDFont logoFont = loadLogoFont(context);

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float y = context.getYPosition();
        float headerH = 110;

        // Yellow header background
        context.drawRect(0, y - headerH + style(context).getMargin(), width, headerH,
                PdfStyle.COLOR_HEADER_BG, true);

        // ── LEFT: INVOICE title ──
        float textY = y - 20;
        context.drawText("INVOICE", margin, textY, fontBold, 30, PdfStyle.COLOR_TEXT);
        textY -= 20;

        // Invoice number
        String invNum = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "DRAFT";
        context.drawText("No: " + invNum, margin, textY, fontRegular, 10, PdfStyle.COLOR_TEXT);
        textY -= 16;

        // Issue date
        String issueDate = invoice.getCreatedAt() != null
                ? invoice.getCreatedAt().format(DATE_FMT)
                : "N/A";
        context.drawText("Issue Date:  " + issueDate, margin, textY, fontRegular, 10, PdfStyle.COLOR_TEXT);
        textY -= 16;

        // Due date
        String dueDate = invoice.getDueDate() != null
                ? invoice.getDueDate().format(DATE_FMT)
                : "N/A";
        context.drawText("Due Date:    " + dueDate, margin, textY, fontRegular, 10, PdfStyle.COLOR_TEXT);

        // ── RIGHT: Company branding ──
        float rightX = width - margin;
        float rightY = y - 15;

        // Logo (top-right)
        if (logoBytes != null) {
            context.drawImage(logoBytes, rightX - 60, rightY - 35, 60, 35);
            rightY -= 42;
        }

        // "Powered Via" — small muted label
        context.drawRightAlignedText("Powered Via", rightX, rightY, fontRegular, 7, PdfStyle.COLOR_MUTED);
        rightY -= 13;

        // "FreelanceFlow" — brand name using logoFont
        String brand = (manager != null && manager.getCompanyName() != null
                && !manager.getCompanyName().isBlank())
                        ? manager.getCompanyName()
                        : "FreelanceFlow";
        context.drawRightAlignedText(brand, rightX, rightY, logoFont, 14, PdfStyle.COLOR_TEXT);
        rightY -= 14;

        // Company email
        String email = (manager != null && manager.getUser() != null
                && manager.getUser().getEmail() != null)
                        ? manager.getUser().getEmail()
                        : "contact@freelanceflow.io";
        context.drawRightAlignedText(email, rightX, rightY, fontRegular, 9, PdfStyle.COLOR_TEXT);

        context.setYPosition(y - headerH - 20);
    }

    /**
     * Billing section: "BILL TO" (client) on the left, invoice meta on the right.
     * Section labels in yellow (#F9DC5C), all other text black.
     */
    protected void drawBillingSection(PdfGenerationContext context, Invoice invoice, Manager manager)
            throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float y = context.getYPosition();

        // ── LEFT: BILL TO ──
        float leftY = y;
        context.drawText("BILLED TO", margin, leftY, fontBold, 8, PdfStyle.COLOR_SECTION_LABEL);
        leftY -= 16;

        com.freelance.freelancepm.model.Client client = invoice.getClient();
        if (client != null) {
            String clientName = nonEmpty(client.getName(), "Client Name");
            context.drawText(clientName, margin, leftY, fontBold, 11, PdfStyle.COLOR_TEXT);
            leftY -= 14;

            String address = nonEmpty(client.getAddress(), "");
            if (!address.isEmpty()) {
                context.drawText(address, margin, leftY, fontRegular, 9, PdfStyle.COLOR_TEXT);
                leftY -= 13;
            }

            String phone = nonEmpty(client.getPhone(), "");
            if (!phone.isEmpty()) {
                context.drawText(phone, margin, leftY, fontRegular, 9, PdfStyle.COLOR_TEXT);
                leftY -= 13;
            }

            String email = nonEmpty(client.getEmail(), "");
            if (!email.isEmpty()) {
                context.drawText(email, margin, leftY, fontRegular, 9, PdfStyle.COLOR_TEXT);
                leftY -= 13;
            }
        } else {
            leftY -= 52;
        }

        // ── RIGHT: PAYMENT INFO ──
        float rightX = width - margin;
        float rightY = y;

        context.drawRightAlignedText("PAYMENT INFO", rightX, rightY, fontBold, 8, PdfStyle.COLOR_SECTION_LABEL);
        rightY -= 16;

        // Status badge (orange)
        String status = invoice.getStatus() != null ? invoice.getStatus().name() : "DRAFT";
        context.drawRightAlignedText("Status:  " + status, rightX, rightY, fontBold, 10, PdfStyle.COLOR_STATUS);
        rightY -= 14;

        // Project references (multiple)
        if (invoice.getProjects() != null && !invoice.getProjects().isEmpty()) {
            for (com.freelance.freelancepm.entity.Project project : invoice.getProjects()) {
                String projName = nonEmpty(project.getName(), "Project #" + project.getId());
                context.drawRightAlignedText("Project: " + projName, rightX, rightY, fontRegular, 9, PdfStyle.COLOR_TEXT);
                rightY -= 13;
            }
        }

        context.setYPosition(Math.min(leftY, rightY) - 25);
    }

    /**
     * Line items table.
     * Header row background: #FCEFB4. All text: black.
     */
    protected void drawTable(PdfGenerationContext context, List<InvoiceLineItem> lineItems) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float rightEdge = width - margin;
        float y = context.getYPosition();

        float colItemX = margin;
        float colDescX = margin + 40;
        float colQtyX = rightEdge - 180;
        float colPriceX = rightEdge - 90;
        float colAmountX = rightEdge;

        // Table header row — #FCEFB4 background
        float rowH = 20;
        context.drawRect(margin, y - rowH + 12, rightEdge - margin, rowH,
                PdfStyle.COLOR_TABLE_HEADER, true);

        context.drawText("Item", colItemX, y, fontBold, 9, PdfStyle.COLOR_TEXT);
        context.drawText("Description", colDescX, y, fontBold, 9, PdfStyle.COLOR_TEXT);
        context.drawRightAlignedText("Qty", colQtyX, y, fontBold, 9, PdfStyle.COLOR_TEXT);
        context.drawRightAlignedText("Unit Price", colPriceX, y, fontBold, 9, PdfStyle.COLOR_TEXT);
        context.drawRightAlignedText("Amount", colAmountX, y, fontBold, 9, PdfStyle.COLOR_TEXT);
        y -= rowH;

        // Divider below header
        context.drawLine(margin, y + 10, rightEdge, y + 10, PdfStyle.COLOR_BORDER, 1f);
        y -= 10;

        // Data rows
        if (lineItems != null) {
            int idx = 1;
            for (InvoiceLineItem item : lineItems) {
                context.setYPosition(y);
                context.ensureSpace(30, (ctx) -> {
                    float hy = ctx.getYPosition();
                    ctx.drawRect(margin, hy - rowH + 12, rightEdge - margin, rowH,
                            PdfStyle.COLOR_TABLE_HEADER, true);
                    ctx.drawText("Item", colItemX, hy, fontBold, 9, PdfStyle.COLOR_TEXT);
                    ctx.drawText("Description", colDescX, hy, fontBold, 9, PdfStyle.COLOR_TEXT);
                    ctx.drawRightAlignedText("Qty", colQtyX, hy, fontBold, 9, PdfStyle.COLOR_TEXT);
                    ctx.drawRightAlignedText("Unit Price", colPriceX, hy, fontBold, 9, PdfStyle.COLOR_TEXT);
                    ctx.drawRightAlignedText("Amount", colAmountX, hy, fontBold, 9, PdfStyle.COLOR_TEXT);
                    ctx.setYPosition(hy - rowH);
                    ctx.drawLine(margin, ctx.getYPosition() + 8, rightEdge, ctx.getYPosition() + 8,
                            PdfStyle.COLOR_BORDER, 1f);
                    ctx.setYPosition(ctx.getYPosition() - 8);
                });

                y = context.getYPosition();

                java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                String qtyStr = item.getQuantity() != null ? item.getQuantity().toString() : "0";
                String priceStr = item.getUnitPrice() != null ? "$" + df.format(item.getUnitPrice()) : "$0.00";
                String amtStr = item.getAmount() != null ? "$" + df.format(item.getAmount()) : "$0.00";
                String desc = item.getDescription() != null ? item.getDescription() : "";

                context.drawText(String.valueOf(idx++), colItemX, y, fontRegular, 9, PdfStyle.COLOR_TEXT);

                List<String> lines = context.parseLines(desc, colQtyX - colDescX - 20, fontRegular, 9);
                float descY = y;
                for (String line : lines) {
                    context.drawText(line, colDescX, descY, fontRegular, 9, PdfStyle.COLOR_TEXT);
                    descY -= 12;
                }

                context.drawRightAlignedText(qtyStr, colQtyX, y, fontRegular, 9, PdfStyle.COLOR_TEXT);
                context.drawRightAlignedText(priceStr, colPriceX, y, fontRegular, 9, PdfStyle.COLOR_TEXT);
                context.drawRightAlignedText(amtStr, colAmountX, y, fontRegular, 9, PdfStyle.COLOR_TEXT);

                float rh = Math.max(20, lines.size() * 12 + 8);
                y -= rh;

                context.drawLine(margin, y + 8, rightEdge, y + 8, PdfStyle.COLOR_BORDER, 0.5f);
            }
        }

        context.setYPosition(y - 15);
    }

    /**
     * Totals: Notes on left, Subtotal / Total box on right.
     * Total box background: #FAE588. All text black.
     */
    protected void drawTotalsSection(PdfGenerationContext context, Invoice invoice) throws IOException {
        context.ensureSpace(130, null);

        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float rightEdge = width - margin;
        float y = context.getYPosition();

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
        String subtotalStr = invoice.getSubtotal() != null ? "$" + df.format(invoice.getSubtotal()) : "$0.00";
        String taxStr = invoice.getTax() != null ? "$" + df.format(invoice.getTax()) : "$0.00";
        String totalStr = invoice.getTotal() != null ? "$" + df.format(invoice.getTotal()) : "$0.00";

        // ── LEFT: Notes ──
        float leftY = y;
        context.drawText("NOTES:", margin, leftY, fontBold, 9, PdfStyle.COLOR_SECTION_LABEL);
        leftY -= 14;

        String notes = invoice.getDescription();
        if (notes != null && !notes.isBlank()) {
            List<String> lines = context.parseLines(notes, 250, fontRegular, 9);
            for (String line : lines) {
                context.drawText(line, margin, leftY, fontRegular, 9, PdfStyle.COLOR_TEXT);
                leftY -= 12;
            }
        } else {
            context.drawText("Thank you for your business.", margin, leftY, fontRegular, 9, PdfStyle.COLOR_TEXT);
            leftY -= 12;
        }

        // ── RIGHT: Subtotal / Tax / Total ──
        float rightY = y;
        float labelX = rightEdge - 160;
        float boxW = 170;
        float boxH = 70;

        context.drawText("Subtotal", labelX, rightY, fontRegular, 9, PdfStyle.COLOR_TEXT);
        context.drawRightAlignedText(subtotalStr, rightEdge, rightY, fontRegular, 9, PdfStyle.COLOR_TEXT);
        rightY -= 16;

        context.drawText("Tax (10%)", labelX, rightY, fontRegular, 9, PdfStyle.COLOR_TEXT);
        context.drawRightAlignedText(taxStr, rightEdge, rightY, fontRegular, 9, PdfStyle.COLOR_TEXT);
        rightY -= 20;

        // Total box with yellow background
        context.drawRect(labelX - 10, rightY - 14, boxW + 10, 30,
                PdfStyle.COLOR_HEADER_BG, true);

        context.drawText("TOTAL DUE", labelX, rightY, fontBold, 12, PdfStyle.COLOR_TEXT);
        context.drawRightAlignedText(totalStr, rightEdge, rightY, fontBold, 14, PdfStyle.COLOR_TEXT);

        context.setYPosition(Math.min(leftY, rightY - 20) - 30);
    }

    /**
     * Footer: footer highlight row in #FCEFB4, "Powered Via / FREELANCEFLOW"
     * branding.
     */
    protected void drawFooter(PdfGenerationContext context, Manager manager) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        org.apache.pdfbox.pdmodel.font.PDFont logoFont = loadLogoFont(context);

        float margin = context.getMargin();
        float width = context.getPageWidth();
        float centerX = width / 2;
        float footerY = 70;

        // Footer highlight bar (#FCEFB4)
        context.drawRect(0, footerY - 5, width, 28, PdfStyle.COLOR_FOOTER_HL, true);

        // "Verified By FreelanceFlow Secure Infrastructure · <InvoiceNumber>"
        String invNum = context.getCurrentPageNumber() + "*";
        String verText = "Verified By FreelanceFlow Secure Infrastructure · " + invNum;
        float verW = fontRegular.getStringWidth(verText) / 1000 * 8;
        float verX = centerX - verW / 2;
        context.drawText(verText, verX, footerY + 5, fontRegular, 8, PdfStyle.COLOR_TEXT);

        // ── Below the bar: Powered Via / FREELANCEFLOW / email ──
        float brandY = footerY - 18;

        // "Powered Via" — small muted
        String poweredVia = "Powered Via";
        float pvW = fontRegular.getStringWidth(poweredVia) / 1000 * 7;
        context.drawText(poweredVia, centerX - pvW / 2, brandY, fontRegular, 7, PdfStyle.COLOR_MUTED);
        brandY -= 12;

        // "FreelanceFlow" — brand name
        String brand = (manager != null && manager.getCompanyName() != null
                && !manager.getCompanyName().isBlank())
                        ? manager.getCompanyName()
                        : "FreelanceFlow";
        float bW = logoFont.getStringWidth(brand) / 1000 * 9;
        context.drawText(brand, centerX - bW / 2, brandY, logoFont, 9, PdfStyle.COLOR_TEXT);
        brandY -= 11;

        // Email
        String email = (manager != null && manager.getUser() != null
                && manager.getUser().getEmail() != null)
                        ? manager.getUser().getEmail()
                        : "contact@freelanceflow.io";
        float eW = fontRegular.getStringWidth(email) / 1000 * 7;
        context.drawText(email, centerX - eW / 2, brandY, fontRegular, 7, PdfStyle.COLOR_TEXT);
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    /** Returns the PdfStyle from the context. */
    private PdfStyle style(PdfGenerationContext ctx) {
        return ctx.getStyle();
    }

    private Manager resolveManager(Invoice invoice) {
        if (invoice.getProjects() != null && !invoice.getProjects().isEmpty() && invoice.getProjects().get(0).getManagerId() != null) {
            return managerRepository.findById(invoice.getProjects().get(0).getManagerId()).orElse(null);
        }
        return null;
    }

    private byte[] fetchLogoBytes(Manager manager) {
        if (manager == null)
            return null;
        String logoUrl = manager.getLogoUrl();
        if (logoUrl == null || logoUrl.isEmpty())
            return null;
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
            try (PDPageContentStream cs = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                float x = page.getMediaBox().getWidth() - style.getMargin() - 60;
                float y = style.getMargin() / 2;
                cs.newLineAtOffset(x, y);
                cs.showText("Page " + (i + 1) + " of " + totalPages);
                cs.endText();
            }
        }
    }

    /** Returns value if non-null/non-blank, otherwise fallback. */
    private String nonEmpty(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    private org.apache.pdfbox.pdmodel.font.PDFont loadLogoFont(PdfGenerationContext context) throws IOException {
        try (java.io.InputStream is = getClass().getResourceAsStream("/fonts/gebuk.ttf")) {
            if (is == null) {
                return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            }
            try (java.io.InputStream patchedIs = com.freelance.freelancepm.util.FontUtils.patchFont(is)) {
                return org.apache.pdfbox.pdmodel.font.PDType0Font.load(context.getDocument(), patchedIs);
            } catch (Exception e) {
                return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            }
        }
    }
}
