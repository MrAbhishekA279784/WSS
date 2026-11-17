package com.womensafety.ui.components;

import com.womensafety.ui.Theme;

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom rounded pill button supporting primary peach, outlined peach, and dark charcoal variants.
 */
public class PrimaryButton extends JButton {

    public enum Style {
        PRIMARY_PEACH,
        OUTLINED_PEACH,
        SECONDARY_DARK,
        EMERGENCY_OUTLINE
    }

    private Style style;
    private boolean hovered = false;
    private boolean pressed = false;
    private int cornerRadius = Theme.PILL_RADIUS * 2; // full pill by default

    public PrimaryButton(String text, Style style) {
        this(text, style, null);
    }

    public PrimaryButton(String text, Style style, Icon icon) {
        super(text);
        this.style = style;
        if (icon != null) {
            setIcon(icon);
            setIconTextGap(10);
        }
        setFont(Theme.bold(14f));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(280, 48));
        setMinimumSize(new Dimension(120, 46));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        applyTextColor();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    public void setStyle(Style style) {
        this.style = style;
        applyTextColor();
        repaint();
    }

    private void applyTextColor() {
        switch (style) {
            case PRIMARY_PEACH -> setForeground(Theme.TEXT_ON_PEACH);
            case OUTLINED_PEACH, SECONDARY_DARK -> setForeground(Theme.TEXT_PRIMARY);
            case EMERGENCY_OUTLINE -> setForeground(Theme.EMERGENCY);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double w = getWidth();
        double h = getHeight();
        double arc = Math.min(cornerRadius, h);
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1, w - 2, h - 2, arc, arc);

        if (!isEnabled()) {
            g2.setColor(Theme.BG_CARD);
            g2.fill(shape);
            g2.setColor(Theme.BORDER_SUBTLE);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(shape);
        } else {
            switch (style) {
                case PRIMARY_PEACH -> {
                    Color bg = pressed ? Theme.PEACH_GLOW : (hovered ? Theme.PEACH_SECONDARY : Theme.PEACH_PRIMARY);
                    g2.setColor(bg);
                    g2.fill(shape);
                }
                case OUTLINED_PEACH -> {
                    Color bg = pressed ? Theme.BG_CARD : (hovered ? new Color(0x16, 0x12, 0x12) : Theme.BG_MAIN);
                    g2.setColor(bg);
                    g2.fill(shape);
                    g2.setColor(hovered ? Theme.PEACH_PRIMARY : Theme.PEACH_GLOW);
                    g2.setStroke(new BasicStroke(1.3f));
                    g2.draw(shape);
                }
                case SECONDARY_DARK -> {
                    Color bg = pressed ? Theme.BG_ELEVATED : (hovered ? Theme.BG_CARD : new Color(0x13, 0x13, 0x13));
                    g2.setColor(bg);
                    g2.fill(shape);
                    g2.setColor(hovered ? Theme.TEXT_SECONDARY : new Color(0x52, 0x4D, 0x4D));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(shape);
                }
                case EMERGENCY_OUTLINE -> {
                    Color bg = hovered ? Theme.EMERGENCY_BG : Theme.BG_MAIN;
                    g2.setColor(bg);
                    g2.fill(shape);
                    g2.setColor(Theme.EMERGENCY);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(shape);
                }
            }
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
