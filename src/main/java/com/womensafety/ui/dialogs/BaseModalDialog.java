package com.womensafety.ui.dialogs;

import com.womensafety.ui.Theme;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;

/**
 * Base styled modal dialog matching the dark charcoal and peach palette.
 */
public class BaseModalDialog extends JDialog {

    protected final RoundedPanel rootCard;
    protected final JPanel bodyPanel;

    public BaseModalDialog(Frame owner, String title, int width) {
        super(owner, title, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        rootCard = new RoundedPanel(new BorderLayout(0, 12), 18, Theme.BG_CARD, Theme.BORDER_SUBTLE);
        rootCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        rootCard.setPreferredSize(new Dimension(width, 10)); // height computed dynamically after pack

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.bold(17f));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        rootCard.add(titleLabel, BorderLayout.NORTH);

        bodyPanel = new JPanel();
        bodyPanel.setOpaque(false);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        rootCard.add(bodyPanel, BorderLayout.CENTER);

        setContentPane(rootCard);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JPanel.WHEN_IN_FOCUSED_WINDOW
        );
    }

    protected void finishLayout(int width) {
        rootCard.setPreferredSize(null);
        pack();
        Dimension packed = getPreferredSize();
        setSize(width, Math.max(packed.height, 160));
        setLocationRelativeTo(getOwner());
    }

    protected JLabel createErrorLabel() {
        JLabel err = new JLabel(" ");
        err.setFont(Theme.regular(12f));
        err.setForeground(Theme.EMERGENCY);
        err.setAlignmentX(Component.LEFT_ALIGNMENT);
        return err;
    }

    protected JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.regular(12f));
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static void showMessage(Frame owner, String title, String message, boolean isError) {
        BaseModalDialog dialog = new BaseModalDialog(owner, title, 340);

        JLabel msg = new JLabel("<html><body style='width:270px;'>" + escapeHtml(message) + "</body></html>");
        msg.setFont(Theme.regular(13f));
        msg.setForeground(isError ? Theme.EMERGENCY : Theme.TEXT_SECONDARY);
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        dialog.bodyPanel.add(msg);
        dialog.bodyPanel.add(Box.createVerticalStrut(18));

        PrimaryButton okBtn = new PrimaryButton("OK", PrimaryButton.Style.PRIMARY_PEACH);
        okBtn.setPreferredSize(new Dimension(120, 40));
        okBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        okBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        okBtn.addActionListener(e -> dialog.dispose());
        dialog.bodyPanel.add(okBtn);

        dialog.finishLayout(340);
        dialog.setVisible(true);
    }

    public static boolean showConfirm(Frame owner, String title, String message, String confirmLabel, boolean destructive) {
        final boolean[] confirmed = {false};
        BaseModalDialog dialog = new BaseModalDialog(owner, title, 340);

        JLabel msg = new JLabel("<html><body style='width:270px;'>" + escapeHtml(message) + "</body></html>");
        msg.setFont(Theme.regular(13f));
        msg.setForeground(Theme.TEXT_SECONDARY);
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        dialog.bodyPanel.add(msg);
        dialog.bodyPanel.add(Box.createVerticalStrut(18));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        PrimaryButton cancelBtn = new PrimaryButton("Cancel", PrimaryButton.Style.SECONDARY_DARK);
        cancelBtn.setPreferredSize(new Dimension(110, 40));
        cancelBtn.addActionListener(e -> dialog.dispose());

        PrimaryButton confirmBtn = new PrimaryButton(
                confirmLabel,
                destructive ? PrimaryButton.Style.EMERGENCY_OUTLINE : PrimaryButton.Style.PRIMARY_PEACH
        );
        confirmBtn.setPreferredSize(new Dimension(130, 40));
        confirmBtn.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);
        dialog.bodyPanel.add(btnRow);

        dialog.finishLayout(340);
        dialog.setVisible(true);
        return confirmed[0];
    }

    protected static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");
    }
}
