package com.womensafety.ui.components;

import com.womensafety.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;

/**
 * Reusable top screen header with back chevron button and centered title.
 */
public class ScreenHeader extends JPanel {

    private final JButton backButton;
    private final JLabel titleLabel;

    public ScreenHeader(String title, Runnable onBack) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        backButton = new JButton(VectorIcons.get(VectorIcons.Type.BACK_ARROW, 20, Theme.TEXT_PRIMARY));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(false);
        backButton.setPreferredSize(new Dimension(34, 34));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setToolTipText("Back");
        if (onBack != null) {
            backButton.addActionListener(e -> onBack.run());
        } else {
            backButton.setVisible(false);
        }

        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(Theme.bold(18f));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JPanel rightSpacer = new JPanel();
        rightSpacer.setOpaque(false);
        rightSpacer.setPreferredSize(new Dimension(34, 34));

        add(backButton, BorderLayout.WEST);
        add(titleLabel, BorderLayout.CENTER);
        add(rightSpacer, BorderLayout.EAST);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
