package com.freelance.freelancepm.service.pdf;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;
import com.freelance.freelancepm.entity.Manager;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import java.io.InputStream;
import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StandardInvoicePdfLayout implements InvoicePdfLayout {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        @Override
        public void drawHeader(PdfGenerationContext context, Invoice invoice, Manager manager, byte[] logoBytes)
                        throws IOException {
                PdfStyle style = context.getStyle();
                float width = context.getPageWidth();
                float margin = context.getMargin();

                // 1. Primary Branding Header Bar (Deep Navy/Slate)
                float headerHeight = 120;
                context.drawRect(0, context.getYPosition() - headerHeight + style.getMargin(), width, headerHeight,
                                style.getPrimaryColor(), true);

                float textY = context.getYPosition() - 25;

                // 2. Custom "Gebuk" Brand Logo
                PDFont logoFont = loadLogoFont(context);
                String brandName = (manager != null && manager.getCompanyName() != null) ? manager.getCompanyName()
                                : "KINGSMAN";

                if (logoBytes != null) {
                        context.drawImage(logoBytes, margin, textY - 20, 60, 40);
                        context.drawText(brandName, margin + 70, textY, logoFont, 24, Color.WHITE);
                } else {
                        context.drawText(brandName, margin, textY, logoFont, 28, Color.WHITE);
                }

                // 3. Right-Aligned Document Label
                context.drawRightAlignedText("INVOICE", width - margin, textY, FONT_BOLD, 28, Color.WHITE);

                textY -= 25;
                // 4. Official Company Info
                String billingFrom = (manager != null && manager.getAddress() != null) ? manager.getAddress()
                                : "Official Sovereign Corporate Headquarters";
                context.drawText(billingFrom, margin, textY, FONT_REGULAR, style.getFontSizeSmall(),
                                PdfStyle.COLOR_MUTED);
                context.drawRightAlignedText("Official Document", width - margin, textY, FONT_BOLD,
                                style.getFontSizeSmall(),
                                PdfStyle.COLOR_MUTED);

                textY -= style.getLineSpacing();
                String contact = (manager != null && manager.getContactNumber() != null) ? manager.getContactNumber()
                                : "finance@kingsman.io";
                context.drawText(contact, margin, textY, FONT_REGULAR, style.getFontSizeSmall(), PdfStyle.COLOR_MUTED);

                context.setYPosition(context.getYPosition() - headerHeight);
        }

        @Override
        public void drawInvoiceInfo(PdfGenerationContext context, Invoice invoice) throws IOException {
                PdfStyle style = context.getStyle();
                float margin = context.getMargin();
                float y = context.getYPosition() - 40;

                context.drawText("INVOICE NO:", margin, y, FONT_BOLD, style.getFontSizeHeader(),
                                style.getPrimaryColor());
                context.drawText(invoice.getInvoiceNumber(), margin + 80, y, FONT_REGULAR, style.getFontSizeHeader(),
                                style.getPrimaryColor());

                float rightColX = context.getPageWidth() - margin - 150;
                context.drawText("DATE:", rightColX, y, FONT_BOLD, style.getFontSizeSmall(), PdfStyle.COLOR_MUTED);
                context.drawText(invoice.getCreatedAt().format(DATE_FORMATTER), rightColX + 40, y, FONT_REGULAR,
                                style.getFontSizeSmall(), PdfStyle.COLOR_SECONDARY);

                y -= style.getLineSpacing();
                context.drawText("DUE DATE:", rightColX, y, FONT_BOLD, style.getFontSizeSmall(), PdfStyle.COLOR_MUTED);
                context.drawText(invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FORMATTER) : "N/A",
                                rightColX + 60, y, FONT_REGULAR, style.getFontSizeSmall(), PdfStyle.COLOR_SECONDARY);

                context.setYPosition(y - 40);
        }

        @Override
        public void drawClientSection(PdfGenerationContext context, Invoice invoice,
                        com.freelance.freelancepm.entity.Manager manager) throws IOException {
                com.freelance.freelancepm.model.Client client = invoice.getClient();
                PdfStyle style = context.getStyle();
                float margin = context.getMargin();
                float y = context.getYPosition();

                if (invoice.getStatus() == InvoiceStatus.DRAFT) {
                        drawDraftWatermark(context);
                }

                // Billed To Column
                context.drawText("BILLED TO", margin, y, FONT_BOLD, style.getFontSizeSmall(), PdfStyle.COLOR_MUTED);
                y -= 15;
                context.drawText(client.getName(), margin, y, FONT_BOLD, style.getFontSizeHeader(),
                                style.getPrimaryColor());
                y -= 12;
                if (client.getAddress() != null) {
                        context.drawWrappedText(client.getAddress(), margin, y, 200, FONT_REGULAR,
                                        style.getFontSizeSmall(),
                                        PdfStyle.COLOR_TEXT);
                }

                // Status / Payment Info Column
                float rightColX = context.getPageWidth() - margin - 150;
                y = context.getYPosition();
                context.drawText("STATUS", rightColX, y, FONT_BOLD, style.getFontSizeSmall(), PdfStyle.COLOR_MUTED);
                y -= 15;

                // draw status box
                String status = invoice.getStatus().getDisplayStatus();
                Color statusColor = invoice.getStatus() == InvoiceStatus.PAID
                                ? new Color(34, 197, 94)
                                : style.getPrimaryColor();

                context.drawRect(rightColX, y - 4, 80, 16, PdfStyle.COLOR_HIGHLIGHT, true);
                context.drawRect(rightColX, y - 4, 80, 16, PdfStyle.COLOR_BORDER, false);
                context.drawText(status, rightColX + 5, y, FONT_BOLD, style.getFontSizeSmall(), statusColor);

                context.setYPosition(y - 60);
        }

        private void drawDraftWatermark(PdfGenerationContext context) throws IOException {
                // Simple watermark in the middle of the page
                // In a real production app, we'd use rotation, but for now we'll just put a big
                // light gray text
                context.drawText("DRAFT - FOR REVIEW ONLY", 150, 400, FONT_BOLD, 40, new Color(240, 240, 240));
        }

        @Override
        public void drawTableHeader(PdfGenerationContext context) throws IOException {
                PdfStyle style = context.getStyle();
                float margin = context.getMargin();
                float width = context.getPageWidth() - (margin * 2);
                float y = context.getYPosition();

                // Header Background
                context.drawRect(margin, y - 5, width, 20, PdfStyle.COLOR_HIGHLIGHT, true);
                context.drawLine(margin, y - 5, margin + width, y - 5, PdfStyle.COLOR_BORDER, 0.5f);
                context.drawLine(margin, y + 15, margin + width, y + 15, PdfStyle.COLOR_BORDER, 0.5f);

                float textY = y + 2;
                context.drawText("DESCRIPTION", margin + 10, textY, FONT_BOLD, style.getFontSizeSmall(),
                                PdfStyle.COLOR_MUTED);
                context.drawRightAlignedText("QTY", margin + width - 160, textY, FONT_BOLD, style.getFontSizeSmall(),
                                PdfStyle.COLOR_MUTED);
                context.drawRightAlignedText("UNIT PRICE", margin + width - 80, textY, FONT_BOLD,
                                style.getFontSizeSmall(),
                                PdfStyle.COLOR_MUTED);
                context.drawRightAlignedText("TOTAL", margin + width - 10, textY, FONT_BOLD, style.getFontSizeSmall(),
                                PdfStyle.COLOR_MUTED);

                context.setYPosition(y - 20);
        }

        @Override
        public void drawTable(PdfGenerationContext context, List<InvoiceLineItem> lineItems) throws IOException {
                PdfStyle style = context.getStyle();
                float margin = context.getMargin();
                float width = context.getPageWidth() - (margin * 2);

                drawTableHeader(context);

                boolean alternate = false;
                for (InvoiceLineItem item : lineItems) {
                        List<String> descLines = context.parseLines(item.getDescription(), 250, FONT_REGULAR,
                                        style.getFontSizeText());
                        float rowHeight = Math.max(25, descLines.size() * 12 + 10);

                        context.ensureSpace(rowHeight, this::drawTableHeader);
                        float y = context.getYPosition();

                        if (alternate) {
                                context.drawRect(margin, y - rowHeight + 15, width, rowHeight, PdfStyle.COLOR_HIGHLIGHT,
                                                true);
                        }

                        float textY = y + 2;
                        context.drawWrappedText(item.getDescription(), margin + 10, textY, 250, FONT_BOLD,
                                        style.getFontSizeText(),
                                        style.getPrimaryColor());

                        context.drawRightAlignedText(String.valueOf(item.getQuantity()), margin + width - 160, textY,
                                        FONT_REGULAR,
                                        style.getFontSizeText(), PdfStyle.COLOR_TEXT);
                        context.drawRightAlignedText("$" + formatMoney(item.getUnitPrice()), margin + width - 80, textY,
                                        FONT_REGULAR, style.getFontSizeText(), PdfStyle.COLOR_TEXT);
                        context.drawRightAlignedText("$" + formatMoney(item.getAmount()), margin + width - 10, textY,
                                        FONT_BOLD,
                                        style.getFontSizeText(), style.getPrimaryColor());

                        context.moveY(rowHeight);
                        alternate = !alternate;
                }

                context.setYPosition(context.getYPosition() - 20);
        }

        @Override
        public void drawTotalSection(PdfGenerationContext context, Invoice invoice) throws IOException {
                PdfStyle style = context.getStyle();
                float margin = context.getMargin();
                float width = context.getPageWidth() - (margin * 2);
                float y = context.getYPosition();

                float labelX = margin + width - 150;
                float valueX = margin + width - 10;

                context.drawLine(labelX, y + 10, valueX, y + 10, PdfStyle.COLOR_BORDER, 0.5f);

                context.drawText("SUBTOTAL", labelX, y, FONT_BOLD, style.getFontSizeSmall(), PdfStyle.COLOR_MUTED);
                context.drawRightAlignedText("$" + formatMoney(invoice.getSubtotal()), valueX, y, FONT_BOLD,
                                style.getFontSizeText(), PdfStyle.COLOR_SECONDARY);

                y -= style.getLineSpacing();
                context.drawText("TAX (10%)", labelX, y, FONT_BOLD, style.getFontSizeSmall(), PdfStyle.COLOR_MUTED);
                context.drawRightAlignedText("$" + formatMoney(invoice.getTax()), valueX, y, FONT_BOLD,
                                style.getFontSizeText(),
                                PdfStyle.COLOR_SECONDARY);

                y -= 30;
                // Total Box
                context.drawRect(labelX - 10, y - 10, 170, 40, style.getPrimaryColor(), true);
                context.drawText("GROSS TOTAL", labelX, y + 5, FONT_BOLD, style.getFontSizeSmall(),
                                PdfStyle.COLOR_MUTED);
                context.drawRightAlignedText("$" + formatMoney(invoice.getTotal()), valueX, y, FONT_BOLD, 18,
                                PdfStyle.COLOR_WHITE);

                context.setYPosition(y - 100);
        }

        @Override
        public void drawFooter(PdfGenerationContext context, com.freelance.freelancepm.entity.Manager manager)
                        throws IOException {
                float margin = context.getMargin();
                float width = context.getPageWidth() - (margin * 2);
                float y = 60;

                context.drawLine(margin, y + 20, margin + width, y + 20, PdfStyle.COLOR_BORDER, 0.5f);

                String contact = manager != null && manager.getContactNumber() != null ? manager.getContactNumber()
                                : "finance@freelanceflow.io";
                context.drawText("Contact: " + contact + " | Verified by FreelanceFlow", margin, y, FONT_BOLD, 7,
                                PdfStyle.COLOR_MUTED);
                context.drawRightAlignedText("Thank you for your business!", margin + width, y, FONT_REGULAR, 8,
                                PdfStyle.COLOR_SECONDARY);

                context.drawText("Terms: Payment due within 30 days. Late fees may apply.", margin, y - 12,
                                FONT_REGULAR, 6,
                                PdfStyle.COLOR_MUTED);
        }

        private String formatMoney(BigDecimal amount) {
                if (amount == null)
                        return "0.00";
                return amount.setScale(2, RoundingMode.HALF_UP).toString();
        }

        private PDFont loadLogoFont(PdfGenerationContext context) throws IOException {
                try (InputStream is = getClass().getResourceAsStream("/fonts/gebuk.ttf")) {
                        if (is == null) {
                                // Return Standard Helvetica Bold if font is missing to prevent crash
                                return FONT_BOLD;
                        }
                        return PDType0Font.load(context.getDocument(), is);
                }
        }
}
