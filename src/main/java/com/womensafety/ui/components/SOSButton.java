package com.womensafety.ui.components;

import com.womensafety.ui.Theme;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Custom-painted circular SOS button with soft radial peach glow and subtle pulse.
 */
public class SOSButton extends JButton {

    private float pulsePhase = 0f;
    private boolean hovered = false;
    private boolean pressed = false;
    private final Timer pulseTimer;

    public SOSButton() {
        super("SOS");
        setPreferredSize(new Dimension(224, 224));
        setMinimumSize(new Dimension(204, 204));
        setMaximumSize(new Dimension(230, 230));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Tap to initiate SOS alert");

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

        pulseTimer = new Timer(45, e -> {
            pulsePhase += 0.08f;
            if (pulsePhase > Math.PI * 2) {
                pulsePhase -= (float) (Math.PI * 2);
            }
            repaint();
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    pulseTimer.start();
                } else {
                    pulseTimer.stop();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight());
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;

        float pulse = (float) (0.5 + 0.5 * Math.sin(pulsePhase));
        double coreRadius = (size * 0.375) * (pressed ? 0.96 : 1.0);
        double glowRadius = coreRadius + 26 + (hovered ? 6 : pulse * 5);

        // Soft radial peach outer glow matching reference Screen 03
        int glowAlpha = hovered ? 128 : (int) (88 + pulse * 24);
        RadialGradientPaint outerGlow = new RadialGradientPaint(
                new Point2D.Double(cx, cy),
                (float) glowRadius,
                new float[]{0.62f, 0.80f, 1.0f},
                new Color[]{
                        new Color(Theme.PEACH_GLOW.getRed(), Theme.PEACH_GLOW.getGreen(), Theme.PEACH_GLOW.getBlue(), glowAlpha),
                        new Color(Theme.PEACH_GLOW.getRed(), Theme.PEACH_GLOW.getGreen(), Theme.PEACH_GLOW.getBlue(), glowAlpha / 3),
                        new Color(Theme.PEACH_GLOW.getRed(), Theme.PEACH_GLOW.getGreen(), Theme.PEACH_GLOW.getBlue(), 0)
                }
        );
        g2.setPaint(outerGlow);
        g2.fill(new Ellipse2D.Double(cx - glowRadius, cy - glowRadius, glowRadius * 2, glowRadius * 2));

        // Dark warm inner circle fill
        RadialGradientPaint innerFill = new RadialGradientPaint(
                new Point2D.Double(cx, cy),
                (float) coreRadius,
                new float[]{0.0f, 0.72f, 1.0f},
                new Color[]{
                        hovered ? new Color(0x3B, 0x2B, 0x29) : new Color(0x32, 0x24, 0x22),
                        new Color(0x21, 0x19, 0x18),
                        new Color(0x16, 0x11, 0x10)
                }
        );
        g2.setPaint(innerFill);
        g2.fill(new Ellipse2D.Double(cx - coreRadius, cy - coreRadius, coreRadius * 2, coreRadius * 2));

        // Luminous peach border ring
        g2.setColor(hovered ? Theme.PEACH_PRIMARY : Theme.PEACH_SECONDARY);
        g2.setStroke(new BasicStroke(2.6f));
        g2.draw(new Ellipse2D.Double(cx - coreRadius, cy - coreRadius, coreRadius * 2, coreRadius * 2));

        // Centered "SOS" label
        g2.setFont(Theme.bold(34f));
        g2.setColor(Theme.TEXT_PRIMARY);
        FontMetrics fm = g2.getFontMetrics();
        String label = "SOS";
        int textX = (int) Math.round(cx - fm.stringWidth(label) / 2.0);
        int textY = (int) Math.round(cy + (fm.getAscent() - fm.getDescent()) / 2.0);
        g2.drawString(label, textX, textY);

        g2.dispose();
    }
}
