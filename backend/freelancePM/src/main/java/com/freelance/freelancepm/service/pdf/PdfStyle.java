package com.freelance.freelancepm.service.pdf;

import lombok.Builder;
import lombok.Getter;
import java.awt.Color;

@Builder
@Getter
public class PdfStyle {
    @Builder.Default
    private final float margin = 50;

    // Font Sizes
    @Builder.Default
    private final float fontSizeHuge = 32;
    @Builder.Default
    private final float fontSizeTitle = 20;
    @Builder.Default
    private final float fontSizeHeader = 12;
    @Builder.Default
    private final float fontSizeText = 10;
    @Builder.Default
    private final float fontSizeSmall = 8;

    @Builder.Default
    private final float lineSpacing = 15;

    // Instance Colors (Configurable)
    @Builder.Default
    private final Color primaryColor = COLOR_PRIMARY;
    @Builder.Default
    private final Color secondaryColor = COLOR_SECONDARY;

    // Static Base Colors (Antigravity Theme Defaults)
    public static final Color COLOR_PRIMARY = new Color(15, 23, 42);     // Slate 900
    public static final Color COLOR_SECONDARY = new Color(51, 65, 85);   // Slate 700
    public static final Color COLOR_TEXT = new Color(71, 85, 105);        // Slate 600
    public static final Color COLOR_MUTED = new Color(148, 163, 184);     // Slate 400
    public static final Color COLOR_BORDER = new Color(226, 232, 240);    // Slate 200
    public static final Color COLOR_HIGHLIGHT = new Color(248, 250, 252); // Slate 50
    public static final Color COLOR_WHITE = Color.WHITE;

    public static PdfStyle defaultStyle() {
        return PdfStyle.builder().build();
    }

    public static PdfStyle fromManager(com.freelance.freelancepm.entity.Manager manager) {
        if (manager == null || manager.getBrandingColor() == null) {
            return defaultStyle();
        }
        try {
            Color color = Color.decode(manager.getBrandingColor());
            return PdfStyle.builder()
                    .primaryColor(color)
                    .build();
        } catch (Exception e) {
            return defaultStyle();
        }
    }
}
