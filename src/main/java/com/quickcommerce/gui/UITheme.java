package com.quickcommerce.gui;

import java.awt.*;

/**
 * Central design token registry for the Quick Commerce GUI.
 * All colours, fonts, and sizing constants live here so the
 * entire UI stays visually consistent.
 */
public final class UITheme {

    private UITheme() {} // utility class

    // -----------------------------------------------------------------------
    // Colour Palette  — Dark "midnight grocery" theme
    // -----------------------------------------------------------------------
    public static final Color BG_DEEP        = new Color(0x0B0F13);
    public static final Color BG_PANEL       = new Color(0x141A21);
    public static final Color BG_CARD        = new Color(0x1C2530);
    public static final Color BG_CARD_HOVER  = new Color(0x222E3C);
    public static final Color BG_INPUT       = new Color(0x111820);
    public static final Color BG_SIDEBAR     = new Color(0x0E1318);

    public static final Color ACCENT_GREEN   = new Color(0x2ECC71);
    public static final Color ACCENT_TEAL    = new Color(0x1ABC9C);
    public static final Color ACCENT_AMBER   = new Color(0xF39C12);
    public static final Color ACCENT_RED     = new Color(0xE74C3C);
    public static final Color ACCENT_BLUE    = new Color(0x3498DB);
    public static final Color ACCENT_PURPLE  = new Color(0x9B59B6);

    public static final Color TEXT_PRIMARY   = new Color(0xECF0F1);
    public static final Color TEXT_SECONDARY = new Color(0x95A5A6);
    public static final Color TEXT_MUTED     = new Color(0x566573);
    public static final Color TEXT_INVERSE   = new Color(0x0B0F13);

    public static final Color BORDER_SUBTLE  = new Color(0x1E2A35);
    public static final Color BORDER_NORMAL  = new Color(0x2C3E50);

    // -----------------------------------------------------------------------
    // Gradient helpers
    // -----------------------------------------------------------------------
    public static GradientPaint accentGradient(int x1, int y1, int x2, int y2) {
        return new GradientPaint(x1, y1, ACCENT_GREEN, x2, y2, ACCENT_TEAL);
    }
    public static GradientPaint cardGradient(int x1, int y1, int x2, int y2) {
        return new GradientPaint(x1, y1, BG_CARD, x2, y2, BG_PANEL);
    }
    public static GradientPaint bgGradient(int x1, int y1, int x2, int y2) {
        return new GradientPaint(x1, y1, BG_DEEP, x2, y2, new Color(0x0F1923));
    }

    // -----------------------------------------------------------------------
    // Typography
    // -----------------------------------------------------------------------
    public static final Font FONT_DISPLAY = new Font("SansSerif", Font.BOLD,  28);
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  18);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD,  14);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN,12);
    public static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  12);

    // -----------------------------------------------------------------------
    // Sizing
    // -----------------------------------------------------------------------
    public static final int  RADIUS_LARGE  = 16;
    public static final int  RADIUS_MEDIUM = 10;
    public static final int  RADIUS_SMALL  = 6;
    public static final int  SIDEBAR_WIDTH = 220;
    public static final Insets CARD_INSETS = new Insets(16, 20, 16, 20);
}
