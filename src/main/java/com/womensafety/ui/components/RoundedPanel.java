package com.womensafety.ui.components;

import com.womensafety.ui.Theme;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable dark charcoal rounded card with subtle 1px border and optional hover state.
 */
public class RoundedPanel extends JPanel {

    private int radius;
    private Color fillColor;
    private Color borderColor;
    private Color hoverFillColor;
    private boolean hovered = false;
    private boolean interactive = false;

    public RoundedPanel(LayoutManager layout) {
        this(layout, Theme.CARD_RADIUS, Theme.BG_CARD_SUBTLE, Theme.BORDER_SUBTLE);
    }

    public RoundedPanel(LayoutManager layout, int radius, Color fillColor, Color borderColor) {
        super(layout);
        this.radius = radius;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.hoverFillColor = Theme.BG_ELEVATED;
        setOpaque(false);
    }

    public void enableHover(Runnable onClick) {
        this.interactive = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double w = getWidth();
        double h = getHeight();
        RoundRectangle2D rect = new RoundRectangle2D.Double(0.5, 0.5, w - 1.0, h - 1.0, radius, radius);

        g2.setColor(interactive && hovered ? hoverFillColor : fillColor);
        g2.fill(rect);

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(rect);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
