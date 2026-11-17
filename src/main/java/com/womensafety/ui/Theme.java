package com.womensafety.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Strict color palette, typography, and FlatLaf configuration matching the reference UI.
 */
public final class Theme {

    // Fixed Reference Color Palette
    public static final Color BG_MAIN = new Color(0x08, 0x08, 0x08);          // #080808
    public static final Color BG_SECONDARY = new Color(0x11, 0x11, 0x11);     // #111111
    public static final Color BG_CARD = new Color(0x1C, 0x1C, 0x1C);          // #1C1C1C
    public static final Color BG_CARD_SUBTLE = new Color(0x16, 0x16, 0x16);   // #161616
    public static final Color BG_ELEVATED = new Color(0x25, 0x25, 0x25);      // #252525

    public static final Color PEACH_PRIMARY = new Color(0xF5, 0xB6, 0xA9);    // #F5B6A9
    public static final Color PEACH_SECONDARY = new Color(0xEC, 0xA9, 0x9C);  // #ECA99C
    public static final Color PEACH_GLOW = new Color(0xE8, 0xA9, 0x9D);       // #E8A99D
    public static final Color PEACH_DARK_TINT = new Color(0x2C, 0x22, 0x20);  // Subtle icon badge bg

    public static final Color TEXT_PRIMARY = new Color(0xF5, 0xF2, 0xF0);     // #F5F2F0
    public static final Color TEXT_SECONDARY = new Color(0xA5, 0xA0, 0xA0);   // #A5A0A0
    public static final Color TEXT_MUTED = new Color(0x77, 0x72, 0x72);       // #777272
    public static final Color TEXT_ON_PEACH = new Color(0x16, 0x12, 0x12);    // Dark text on peach buttons

    public static final Color BORDER_SUBTLE = new Color(0x39, 0x33, 0x33);    // #393333
    public static final Color BORDER_MUTED = new Color(0x2B, 0x27, 0x27);     // #2B2727

    public static final Color SUCCESS = new Color(0x8D, 0xBD, 0x87);          // #8DBD87
    public static final Color SUCCESS_BG = new Color(0x24, 0x33, 0x23);
    public static final Color EMERGENCY = new Color(0xD9, 0x6B, 0x70);        // #D96B70
    public static final Color EMERGENCY_BG = new Color(0x36, 0x1F, 0x20);

    public static final int CARD_RADIUS = 16;
    public static final int INPUT_RADIUS = 14;
    public static final int PILL_RADIUS = 24;
    public static final int SCREEN_PADDING_X = 24;

    private static final String FONT_FAMILY = resolveFontFamily();

    private Theme() {
    }

    private static String resolveFontFamily() {
        try {
            String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            boolean hasInter = Arrays.stream(available).anyMatch(f -> f.equalsIgnoreCase("Inter"));
            if (hasInter) {
                return "Inter";
            }
        } catch (Exception ignored) {
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, Math.round(size));
    }

    public static Font regular(float size) {
        return font(Font.PLAIN, size);
    }

    public static Font bold(float size) {
        return font(Font.BOLD, size);
    }

    public static void setupLookAndFeel() {
        FlatDarkLaf.setup();
        UIManager.put("Panel.background", BG_MAIN);
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", PEACH_PRIMARY);
        UIManager.put("PasswordField.caretForeground", PEACH_PRIMARY);
        UIManager.put("ScrollBar.thumb", BORDER_SUBTLE);
        UIManager.put("ScrollBar.track", BG_MAIN);
        UIManager.put("ScrollBar.width", 6);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("defaultFont", regular(13f));
    }
}
