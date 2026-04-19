package com.freelance.freelancepm.service.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Color;
import java.math.RoundingMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import com.freelance.freelancepm.dto.ReportResponse;
import com.freelance.freelancepm.service.ReportPdfService.ProjectRevenueDetails;
import com.freelance.freelancepm.entity.Manager;

@Component
public class StandardReportPdfLayout implements ReportPdfLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final Color CORPORATE_BLUE = new Color(37, 99, 235);

    @Override
    public void drawHeader(PdfGenerationContext context, LocalDate startDate, LocalDate endDate, Manager manager,
            byte[] logoBytes)
            throws IOException {
        PdfStyle style = context.getStyle();
        float width = context.getPageWidth();
        float margin = context.getMargin();
        float headerHeight = 100;
        context.drawRect(0, context.getYPosition() - headerHeight + style.getMargin(), width, headerHeight,
                style.getPrimaryColor(), true);

        float textY = context.getYPosition() - 25;

        PDFont gebukFont = loadLogoFont(context);
        context.drawText("FreeLanceFlow", margin, textY, gebukFont, 26, Color.WHITE);
        context.drawRightAlignedText("FINANCIAL PERFORMANCE", width - margin, textY, FONT_BOLD, 16, Color.WHITE);

        textY -= 20;
        String dateRange = "Period: " + startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER);
        context.drawRightAlignedText(dateRange, width - margin, textY, FONT_REGULAR, 8, PdfStyle.COLOR_MUTED);

        context.setYPosition(context.getYPosition() - headerHeight - 30);
    }

    @Override
    public void drawSummaryCards(PdfGenerationContext context, ReportResponse report) throws IOException {
        PdfStyle style = context.getStyle();
        float margin = context.getMargin();
        float cardWidth = (context.getPageWidth() - (margin * 2) - 20) / 3;
        float y = context.getYPosition();

        drawCard(context, "TOTAL REVENUE", report.getTotalRevenue(), margin, y, cardWidth, style.getPrimaryColor());
        drawCardNumber(context, "PROJECTS COMPLETED", report.getProjectsCompleted(), margin + cardWidth + 10, y,
                cardWidth,
                style.getSecondaryColor());
        drawCardNumber(context, "INVOICES GENERATED", report.getInvoicesGenerated(), margin + (cardWidth + 10) * 2, y,
                cardWidth,
                CORPORATE_BLUE);

        context.setYPosition(y - 80);
    }

    private void drawCard(PdfGenerationContext context, String label, BigDecimal amount, float x, float y, float w,
            Color valueColor) throws IOException {

        context.drawRect(x, y - 55, w, 65, PdfStyle.COLOR_HIGHLIGHT, true);
        context.drawRect(x, y - 55, w, 65, PdfStyle.COLOR_BORDER, false);

        context.drawText(label, x + 12, y - 10, FONT_BOLD, 7, PdfStyle.COLOR_MUTED);
        context.drawText("$" + formatMoney(amount), x + 12, y - 40, FONT_BOLD, 15, valueColor);
    }

    private void drawCardNumber(PdfGenerationContext context, String label, long amount, float x, float y, float w,
            Color valueColor) throws IOException {

        context.drawRect(x, y - 55, w, 65, PdfStyle.COLOR_HIGHLIGHT, true);
        context.drawRect(x, y - 55, w, 65, PdfStyle.COLOR_BORDER, false);

        context.drawText(label, x + 12, y - 10, FONT_BOLD, 7, PdfStyle.COLOR_MUTED);
        context.drawText(String.valueOf(amount), x + 12, y - 40, FONT_BOLD, 15, valueColor);
    }

    @Override
    public void drawBreakdownTable(PdfGenerationContext context, List<ProjectRevenueDetails> details)
            throws IOException {
        PdfStyle style = context.getStyle();
        float margin = context.getMargin();
        float width = context.getPageWidth() - (margin * 2);

        context.drawText("PROJECT-WISE BREAKDOWN", margin, context.getYPosition(), FONT_BOLD, 10,
                style.getSecondaryColor());
        context.moveY(20);

        drawTableHeader(context);

        boolean alternate = false;
        for (ProjectRevenueDetails detail : details) {
            context.ensureSpace(25, this::drawTableHeader);
            float y = context.getYPosition();
            if (alternate) {
                context.drawRect(margin, y - 15, width, 25, PdfStyle.COLOR_HIGHLIGHT, true);
            }

            float textY = y + 2;
            context.drawText(detail.projectName(), margin + 10, textY, FONT_BOLD, style.getFontSizeText(),
                    style.getPrimaryColor());
            context.drawText(detail.clientName(), margin + 200, textY, FONT_REGULAR, style.getFontSizeText(),
                    PdfStyle.COLOR_TEXT);

            context.drawRightAlignedText("$" + formatMoney(detail.revenue()), margin + width - 10, textY, FONT_BOLD,
                    style.getFontSizeText(), CORPORATE_BLUE);

            context.moveY(25);
            alternate = !alternate;
        }
    }

    private void drawTableHeader(PdfGenerationContext context) throws IOException {
        PdfStyle style = context.getStyle();
        float margin = context.getMargin();
        float width = context.getPageWidth() - (margin * 2);
        float y = context.getYPosition();

        context.drawRect(margin, y - 5, width, 20, PdfStyle.COLOR_BORDER, true);

        float textY = y + 2;
        context.drawText("PROJECT", margin + 10, textY, FONT_BOLD, 8, Color.WHITE);
        context.drawText("CLIENT", margin + 200, textY, FONT_BOLD, 8, Color.WHITE);
        context.drawRightAlignedText("REVENUE", margin + width - 10, textY, FONT_BOLD, 8, Color.WHITE);

        context.setYPosition(y - 20);
    }

    @Override
    public void drawFooter(PdfGenerationContext context, Manager manager) throws IOException {
        float margin = context.getMargin();
        float width = context.getPageWidth() - (margin * 2);
        float y = 50;

        context.drawLine(margin, y + 15, margin + width, y + 15, PdfStyle.COLOR_BORDER, 0.5f);

        String brand = (manager != null) ? manager.getCompanyName() : "FreelanceFlow";
        context.drawText(brand + " Confidential Financial Audit", margin, y, FONT_BOLD, 7, PdfStyle.COLOR_MUTED);
        context.drawRightAlignedText("Page " + context.getCurrentPageNumber(), margin + width, y, FONT_REGULAR, 7,
                PdfStyle.COLOR_MUTED);
    }

    private PDFont loadLogoFont(PdfGenerationContext context) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/fonts/gebuk.ttf")) {
            if (is == null)
                return FONT_BOLD;
            try {
                return PDType0Font.load(context.getDocument(), is);
            } catch (IOException e) {
                return FONT_BOLD;
            }
        }
    }

    private String formatMoney(BigDecimal amount) {
        return (amount != null) ? amount.setScale(2, RoundingMode.HALF_UP).toString() : "0.00";
    }
}