package com.freelance.freelancepm.service.pdf;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PdfStyle {
    @Builder.Default
    private final float margin = 50;
    @Builder.Default
    private final float fontSizeTitle = 20;
    @Builder.Default
    private final float fontSizeHeader = 12;
    @Builder.Default
    private final float fontSizeText = 10;
    @Builder.Default
    private final float lineSpacing = 15;

    public static PdfStyle defaultStyle() {
        return PdfStyle.builder().build();
    }
}
