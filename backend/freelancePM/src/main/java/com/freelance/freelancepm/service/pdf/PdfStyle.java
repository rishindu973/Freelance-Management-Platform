package com.freelance.freelancepm.service.pdf;

import com.freelance.freelancepm.entity.Manager;
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
    private final Color primaryColor = COLOR_HEADER_BG;
    @Builder.Default
    private final Color secondaryColor = COLOR_SECTION_LABEL;

    // ─────────────────────────────────────────────────────────────
    // Warm Yellow Invoice Palette
    // ─────────────────────────────────────────────────────────────

    /** Header bar and Total box background — soft pastel yellow */
    public static final Color COLOR_HEADER_BG   = new Color(0xFA, 0xE5, 0x88); // #FAE588

    /** Section labels ("BILL TO", "PAYMENT INFO") */
    public static final Color COLOR_SECTION_LABEL = new Color(0xF9, 0xDC, 0x5C); // #F9DC5C

    /** Table header row background */
    public static final Color COLOR_TABLE_HEADER = new Color(0xFC, 0xEF, 0xB4); // #FCEFB4

    /** Footer verified-line highlight background */
    public static final Color COLOR_FOOTER_HL   = new Color(0xFC, 0xEF, 0xB4); // #FCEFB4

    /** Invoice status badge — warm orange */
    public static final Color COLOR_STATUS      = new Color(0xFF, 0xA7, 0x26); // #FFA726

    /** All primary body text — pure black */
    public static final Color COLOR_TEXT        = new Color(0x00, 0x00, 0x00); // #000000

    /** "OFFICIAL DOCUMENT" secondary muted label */
    public static final Color COLOR_MUTED       = new Color(0xBD, 0xBD, 0xBD); // #BDBDBD

    /** Borders / divider lines */
    public static final Color COLOR_BORDER      = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0

    /** Pure white (for any fills that stay white) */
    public static final Color COLOR_WHITE       = Color.WHITE;

    // Keep legacy aliases for any references outside this file
    /** @deprecated Use COLOR_HEADER_BG */
    @Deprecated public static final Color COLOR_PRIMARY   = COLOR_HEADER_BG;
    /** @deprecated Use COLOR_TEXT */
    @Deprecated public static final Color COLOR_SECONDARY = COLOR_SECTION_LABEL;
    /** @deprecated Use COLOR_MUTED */
    @Deprecated public static final Color COLOR_HIGHLIGHT = new Color(0xFA, 0xE5, 0x88);

    // ─────────────────────────────────────────────────────────────

    public static PdfStyle defaultStyle() {
        return PdfStyle.builder().build();
    }

    public static PdfStyle fromManager(Manager manager) {
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
