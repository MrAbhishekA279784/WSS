package com.womensafety.ui.components;

import com.womensafety.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom rounded input container with left vector icon, placeholder support,
 * and optional password visibility toggle matching Screen 02 of the reference UI.
 */
public class IconInputField extends JPanel {

    private final JTextField textField;
    private final JPasswordField passwordField;
    private final boolean isPassword;
    private boolean focused = false;
    private boolean passwordVisible = false;

    public IconInputField(VectorIcons.Type iconType, String placeholder, boolean isPassword) {
        super(new BorderLayout(10, 0));
        this.isPassword = isPassword;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 12));
        setPreferredSize(new Dimension(280, 48));
        setMinimumSize(new Dimension(120, 48));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel iconLabel = new JLabel(VectorIcons.get(iconType, 18, Theme.TEXT_SECONDARY));
        add(iconLabel, BorderLayout.WEST);

        if (isPassword) {
            this.textField = null;
            this.passwordField = new JPasswordField();
            passwordField.setOpaque(false);
            passwordField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            passwordField.setFont(Theme.regular(13.5f));
            passwordField.setForeground(Theme.TEXT_PRIMARY);
            passwordField.setCaretColor(Theme.PEACH_PRIMARY);
            passwordField.putClientProperty("JTextField.placeholderText", placeholder);
            passwordField.setEchoChar('•');
            passwordField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
            add(passwordField, BorderLayout.CENTER);

            JButton eyeButton = new JButton(VectorIcons.get(VectorIcons.Type.EYE_OFF, 18, Theme.TEXT_SECONDARY));
            eyeButton.setBorderPainted(false);
            eyeButton.setContentAreaFilled(false);
            eyeButton.setFocusPainted(false);
            eyeButton.setOpaque(false);
            eyeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eyeButton.setPreferredSize(new Dimension(28, 28));
            eyeButton.setToolTipText("Show or hide password");
            eyeButton.addActionListener(e -> {
                passwordVisible = !passwordVisible;
                passwordField.setEchoChar(passwordVisible ? (char) 0 : '•');
                eyeButton.setIcon(VectorIcons.get(
                        passwordVisible ? VectorIcons.Type.EYE : VectorIcons.Type.EYE_OFF,
                        18,
                        passwordVisible ? Theme.PEACH_PRIMARY : Theme.TEXT_SECONDARY
                ));
            });
            add(eyeButton, BorderLayout.EAST);
        } else {
            this.passwordField = null;
            this.textField = new JTextField();
            textField.setOpaque(false);
            textField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            textField.setFont(Theme.regular(13.5f));
            textField.setForeground(Theme.TEXT_PRIMARY);
            textField.setCaretColor(Theme.PEACH_PRIMARY);
            textField.putClientProperty("JTextField.placeholderText", placeholder);
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
            add(textField, BorderLayout.CENTER);
        }
    }

    public String getText() {
        if (isPassword) {
            return new String(passwordField.getPassword());
        }
        return textField.getText();
    }

    public void setText(String value) {
        if (isPassword) {
            passwordField.setText(value);
        } else {
            textField.setText(value);
        }
    }

    public void clear() {
        setText("");
    }

    public void addActionListener(java.awt.event.ActionListener listener) {
        if (isPassword) {
            passwordField.addActionListener(listener);
        } else {
            textField.addActionListener(listener);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D rect = new RoundRectangle2D.Double(
                0.5, 0.5, getWidth() - 1.0, getHeight() - 1.0, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        g2.setColor(Theme.BG_CARD);
        g2.fill(rect);

        g2.setColor(focused ? Theme.PEACH_GLOW : Theme.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(focused ? 1.3f : 1.0f));
        g2.draw(rect);

        g2.dispose();
        super.paintComponent(g);
    }
}
