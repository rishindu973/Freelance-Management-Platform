package com.freelance.freelancepm.service.pdf;

import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

@Getter
@Setter
public class PdfGenerationContext {
    private final PDDocument document;
    private final PDPageContentStream contentStream;
    private final PdfStyle style;
    private float yPosition;

    public PdfGenerationContext(PDDocument document, PDPageContentStream contentStream, PdfStyle style) {
        this.document = document;
        this.contentStream = contentStream;
        this.style = style;
        this.yPosition = PDRectangle.A4.getHeight() - style.getMargin();
    }

    public void moveY(float amount) {
        this.yPosition -= amount;
    }

    public float getMargin() {
        return style.getMargin();
    }
}
