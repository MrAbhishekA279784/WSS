package com.womensafety.ui.components;

import com.womensafety.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/**
 * Emergency service card matching Screen 08 of the reference UI.
 */
public class EmergencyCard extends RoundedPanel {

    public EmergencyCard(VectorIcons.Type iconType,
                         String serviceName,
                         String serviceNumber,
                         Runnable onCallClick) {
        super(new BorderLayout(14, 0), Theme.CARD_RADIUS, Theme.BG_CARD_SUBTLE, Theme.BORDER_SUBTLE);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 16));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        setPreferredSize(new Dimension(340, 74));
        setMinimumSize(new Dimension(240, 74));

        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PEACH_DARK_TINT);
                g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(42, 42));
        badge.add(new JLabel(VectorIcons.get(iconType, 20, Theme.PEACH_PRIMARY), javax.swing.SwingConstants.CENTER), BorderLayout.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(serviceName);
        title.setFont(Theme.bold(14f));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel number = new JLabel(serviceNumber);
        number.setFont(Theme.regular(13f));
        number.setForeground(Theme.TEXT_SECONDARY);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(number);

        JLabel callIcon = new JLabel(VectorIcons.get(VectorIcons.Type.PHONE, 19, Theme.PEACH_PRIMARY));

        add(badge, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(callIcon, BorderLayout.EAST);

        enableHover(onCallClick);
    }
}
