package com.womensafety.ui.dialogs;

import com.womensafety.ui.Theme;
import com.womensafety.ui.components.PrimaryButton;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;

/**
 * Confirmation modal before activating SOS to prevent accidental triggers.
 */
public class SOSConfirmationDialog extends BaseModalDialog {

    private boolean confirmed = false;

    public SOSConfirmationDialog(Frame owner) {
        super(owner, "Confirm SOS Alert", 350);

        JLabel info = new JLabel(
                "<html><body style='width:280px;'>This will capture your current location, record an emergency SOS alert in your history, and attempt to notify your trusted contacts if an external gateway is configured.</body></html>"
        );
        info.setFont(Theme.regular(13f));
        info.setForeground(Theme.TEXT_SECONDARY);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        bodyPanel.add(info);
        bodyPanel.add(Box.createVerticalStrut(20));

        JPanel btnGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        btnGrid.setOpaque(false);
        btnGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        PrimaryButton cancelBtn = new PrimaryButton("Cancel", PrimaryButton.Style.SECONDARY_DARK);
        cancelBtn.setPreferredSize(new Dimension(130, 42));
        cancelBtn.addActionListener(e -> dispose());

        PrimaryButton sendBtn = new PrimaryButton("Send SOS", PrimaryButton.Style.PRIMARY_PEACH);
        sendBtn.setPreferredSize(new Dimension(130, 42));
        sendBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        btnGrid.add(cancelBtn);
        btnGrid.add(sendBtn);
        bodyPanel.add(btnGrid);

        finishLayout(350);
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
