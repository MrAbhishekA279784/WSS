package com.womensafety.ui.components;

import com.womensafety.models.EmergencyContact;
import com.womensafety.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/**
 * Trusted contact card matching Screen 06 of the reference UI:
 * circular initial avatar, name, phone number, and peach call button.
 */
public class ContactCard extends RoundedPanel {

    public ContactCard(EmergencyContact contact,
                       Runnable onCardClick,
                       Runnable onCallClick) {
        super(new BorderLayout(14, 0), Theme.CARD_RADIUS, Theme.BG_CARD_SUBTLE, Theme.BORDER_SUBTLE);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        setPreferredSize(new Dimension(340, 70));
        setMinimumSize(new Dimension(240, 70));

        // Circular initial avatar
        JPanel avatar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x32, 0x2C, 0x2C));
                g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(42, 42));
        avatar.setMinimumSize(new Dimension(42, 42));
        avatar.setMaximumSize(new Dimension(42, 42));

        JLabel initialLabel = new JLabel(contact.getInitial(), SwingConstants.CENTER);
        initialLabel.setFont(Theme.bold(15f));
        initialLabel.setForeground(Theme.TEXT_PRIMARY);
        avatar.add(initialLabel, BorderLayout.CENTER);

        // Text block
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(contact.getContactName());
        nameLabel.setFont(Theme.bold(14f));
        nameLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel phoneLabel = new JLabel(contact.getPhoneNumber());
        phoneLabel.setFont(Theme.regular(12.5f));
        phoneLabel.setForeground(Theme.TEXT_SECONDARY);

        textPanel.add(nameLabel);
        textPanel.add(javax.swing.Box.createVerticalStrut(3));
        textPanel.add(phoneLabel);

        // Call icon button
        JButton callBtn = new JButton(VectorIcons.get(VectorIcons.Type.PHONE, 19, Theme.PEACH_PRIMARY));
        callBtn.setBorderPainted(false);
        callBtn.setContentAreaFilled(false);
        callBtn.setFocusPainted(false);
        callBtn.setOpaque(false);
        callBtn.setPreferredSize(new Dimension(36, 36));
        callBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        callBtn.setToolTipText("Call " + contact.getContactName());
        callBtn.addActionListener(e -> {
            if (onCallClick != null) {
                onCallClick.run();
            }
        });

        add(avatar, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(callBtn, BorderLayout.EAST);

        enableHover(onCardClick);
    }
}
