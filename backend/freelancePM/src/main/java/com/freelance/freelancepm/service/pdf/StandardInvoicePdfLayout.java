package com.freelance.freelancepm.service.pdf;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.model.Client;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StandardInvoicePdfLayout implements InvoicePdfLayout {

    @Override
    public void drawHeader(PdfGenerationContext context) throws IOException {
        PDPageContentStream contentStream = context.getContentStream();
        PdfStyle style = context.getStyle();
        float y = context.getYPosition();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), style.getFontSizeTitle());
        contentStream.newLineAtOffset(context.getMargin(), y);
        contentStream.showText("FREELANCEFLOW");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.newLineAtOffset(PDRectangle.A4.getWidth() - context.getMargin() - 70, y);
        contentStream.showText("INVOICE");
        contentStream.endText();

        y -= style.getLineSpacing() * 2;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), style.getFontSizeText());
        contentStream.newLineAtOffset(context.getMargin(), y);
        contentStream.showText("123 Tech Street, Digital City");
        contentStream.newLineAtOffset(0, -style.getLineSpacing());
        contentStream.showText("Email: support@freelanceflow.com");
        contentStream.newLineAtOffset(0, -style.getLineSpacing());
        contentStream.showText("Phone: +1 (555) 012-3456");
        contentStream.endText();

        context.setYPosition(y - (style.getLineSpacing() * 3));
    }

    @Override
    public void drawInvoiceInfo(PdfGenerationContext context, Invoice invoice) throws IOException {
        PDPageContentStream contentStream = context.getContentStream();
        PdfStyle style = context.getStyle();
        float y = context.getYPosition();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), style.getFontSizeHeader());
        contentStream.newLineAtOffset(context.getMargin(), y);
        contentStream.showText("Invoice Number: " + invoice.getInvoiceNumber());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        contentStream.newLineAtOffset(0, -style.getLineSpacing());
        contentStream.showText("Date: " + (invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(formatter) : "N/A"));
        contentStream.newLineAtOffset(0, -style.getLineSpacing());
        contentStream.showText("Due Date: " + (invoice.getDueDate() != null ? invoice.getDueDate().format(formatter) : "N/A"));
        contentStream.newLineAtOffset(0, -style.getLineSpacing());
        contentStream.showText("Status: " + invoice.getStatus());
        contentStream.endText();

        context.setYPosition(y - (style.getLineSpacing() * 5));
    }

    @Override
    public void drawClientSection(PdfGenerationContext context, Client client) throws IOException {
        PDPageContentStream contentStream = context.getContentStream();
        PdfStyle style = context.getStyle();
        float y = context.getYPosition();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), style.getFontSizeHeader());
        contentStream.newLineAtOffset(context.getMargin(), y);
        contentStream.showText("Bill To:");
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), style.getFontSizeText());
        contentStream.newLineAtOffset(0, -style.getLineSpacing());
        contentStream.showText(client.getName());
        contentStream.newLineAtOffset(0, -style.getLineSpacing());
        contentStream.showText(client.getEmail());
        if (client.getAddress() != null && !client.getAddress().isEmpty()) {
            contentStream.newLineAtOffset(0, -style.getLineSpacing());
            contentStream.showText(client.getAddress());
        }
        contentStream.endText();

        context.setYPosition(y - (style.getLineSpacing() * 5));
    }

    @Override
    public void drawTable(PdfGenerationContext context, List<InvoiceLineItem> lineItems) throws IOException {
        PDPageContentStream contentStream = context.getContentStream();
        PdfStyle style = context.getStyle();
        float y = context.getYPosition();

        float col1 = context.getMargin();
        float col2 = 300;
        float col3 = 380;
        float col4 = 500;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), style.getFontSizeText());
        contentStream.newLineAtOffset(col1, y);
        contentStream.showText("Description");
        contentStream.newLineAtOffset(col2 - col1, 0);
        contentStream.showText("Qty");
        contentStream.newLineAtOffset(col3 - col2, 0);
        contentStream.showText("Unit Price");
        contentStream.newLineAtOffset(col4 - col3, 0);
        contentStream.showText("Total");
        contentStream.endText();

        y -= 5;
        contentStream.moveTo(context.getMargin(), y);
        contentStream.lineTo(PDRectangle.A4.getWidth() - context.getMargin(), y);
        contentStream.stroke();
        y -= style.getLineSpacing();

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), style.getFontSizeText());
        for (InvoiceLineItem item : lineItems) {
            contentStream.beginText();
            contentStream.newLineAtOffset(col1, y);
            contentStream.showText(truncate(item.getDescription(), 45));
            contentStream.newLineAtOffset(col2 - col1, 0);
            contentStream.showText(String.valueOf(item.getQuantity()));
            contentStream.newLineAtOffset(col3 - col2, 0);
            contentStream.showText("$" + item.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
            contentStream.newLineAtOffset(col4 - col3, 0);
            contentStream.showText("$" + item.getAmount().setScale(2, RoundingMode.HALF_UP));
            contentStream.endText();
            y -= style.getLineSpacing();
        }

        y -= 5;
        contentStream.moveTo(context.getMargin(), y);
        contentStream.lineTo(PDRectangle.A4.getWidth() - context.getMargin(), y);
        contentStream.stroke();

        context.setYPosition(y - (style.getLineSpacing() * 2));
    }

    @Override
    public void drawTotalSection(PdfGenerationContext context, Invoice invoice) throws IOException {
        PDPageContentStream contentStream = context.getContentStream();
        PdfStyle style = context.getStyle();
        float y = context.getYPosition();

        float xOffset = 380;
        float valueOffset = 500;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), style.getFontSizeText());
        contentStream.newLineAtOffset(xOffset, y);
        contentStream.showText("Subtotal:");
        contentStream.newLineAtOffset(valueOffset - xOffset, 0);
        contentStream.showText("$" + invoice.getSubtotal().setScale(2, RoundingMode.HALF_UP));

        y -= style.getLineSpacing();
        contentStream.newLineAtOffset(-(valueOffset - xOffset), -style.getLineSpacing());
        contentStream.showText("Tax (10%):");
        contentStream.newLineAtOffset(valueOffset - xOffset, 0);
        contentStream.showText("$" + invoice.getTax().setScale(2, RoundingMode.HALF_UP));

        y -= style.getLineSpacing();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), style.getFontSizeHeader());
        contentStream.newLineAtOffset(-(valueOffset - xOffset), -style.getLineSpacing());
        contentStream.showText("Grand Total:");
        contentStream.newLineAtOffset(valueOffset - xOffset, 0);
        contentStream.showText("$" + invoice.getTotal().setScale(2, RoundingMode.HALF_UP));
        contentStream.endText();

        context.setYPosition(y - (style.getLineSpacing() * 4));
    }

    @Override
    public void drawFooter(PdfGenerationContext context) throws IOException {
        PDPageContentStream contentStream = context.getContentStream();
        float y = context.getMargin() + 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        contentStream.newLineAtOffset(context.getMargin(), y);
        contentStream.showText("Payment Terms: Net 30. Please make checks payable to FreelanceFlow.");
        contentStream.newLineAtOffset(0, -10);
        contentStream.showText("Thank you for your business!");
        contentStream.endText();
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
