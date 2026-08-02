package com.freelance.freelancepm.service.pdf;

import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class PdfGenerationContext implements AutoCloseable {
    private final PDDocument document;
    private final PdfStyle style;
    private PDPageContentStream contentStream;
    private float yPosition;
    private int currentPageNumber;

    public PdfGenerationContext(PDDocument document, PdfStyle style) throws IOException {
        this.document = document;
        this.style = style;
        this.currentPageNumber = 0;
        addNewPage();
    }

    public void addNewPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        contentStream = new PDPageContentStream(document, page);
        yPosition = PDRectangle.A4.getHeight() - style.getMargin();
        currentPageNumber++;
    }

    public void drawText(String text, float x, float y, PDFont font, float size, Color color) throws IOException {
        if (text == null) return;
        contentStream.beginText();
        contentStream.setFont(font, size);
        contentStream.setNonStrokingColor(color);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    public void drawRightAlignedText(String text, float x, float y, PDFont font, float size, Color color) throws IOException {
        if (text == null) return;
        float width = font.getStringWidth(text) / 1000 * size;
        drawText(text, x - width, y, font, size, color);
    }

    public void drawWrappedText(String text, float x, float y, float width, PDFont font, float size, Color color) throws IOException {
        if (text == null) return;
        List<String> lines = parseLines(text, width, font, size);
        float currentY = y;
        for (String line : lines) {
            drawText(line, x, currentY, font, size, color);
            currentY -= size * 1.2f;
        }
    }

    public List<String> parseLines(String text, float width, PDFont font, float size) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String trial = currentLine.length() == 0 ? word : currentLine + " " + word;
            float trialWidth = font.getStringWidth(trial) / 1000 * size;
            if (trialWidth > width) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(currentLine.length() == 0 ? "" : " ").append(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    public void drawImage(byte[] imageBytes, float x, float y, float width, float height) throws IOException {
        if (imageBytes == null) return;
        org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject image = org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray(document, imageBytes, "logo");
        contentStream.drawImage(image, x, y, width, height);
    }

    public void drawRect(float x, float y, float width, float height, Color color, boolean fill) throws IOException {
        contentStream.setNonStrokingColor(color);
        contentStream.setStrokingColor(color);
        contentStream.addRect(x, y, width, height);
        if (fill) {
            contentStream.fill();
        } else {
            contentStream.stroke();
        }
    }

    public void drawLine(float x1, float y1, float x2, float y2, Color color, float lineWidth) throws IOException {
        contentStream.setStrokingColor(color);
        contentStream.setLineWidth(lineWidth);
        contentStream.moveTo(x1, y1);
        contentStream.lineTo(x2, y2);
        contentStream.stroke();
    }

    public void ensureSpace(float requiredHeight, OnNewPageCallback callback) throws IOException {
        float footerBuffer = 80; // Increased buffer for footer
        if (yPosition - requiredHeight < footerBuffer) {
            addNewPage();
            if (callback != null) {
                callback.onNewPage(this);
            }
        }
    }

    public void moveY(float amount) {
        this.yPosition -= amount;
    }

    public void setYPosition(float yPosition) {
        this.yPosition = yPosition;
    }

    public float getMargin() {
        return style.getMargin();
    }

    public float getPageWidth() {
        return PDRectangle.A4.getWidth();
    }

    @Override
    public void close() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
    }

    @FunctionalInterface
    public interface OnNewPageCallback {
        void onNewPage(PdfGenerationContext context) throws IOException;
    }
}
