package com.womensafety.ui.dialogs;

import com.womensafety.services.AuthService;
import com.womensafety.services.ServiceResult;
import com.womensafety.ui.Theme;
import com.womensafety.ui.components.IconInputField;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.ui.components.VectorIcons;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;

/**
 * Dialog for resetting an account password verified by registered email in SQLite.
 */
public class ForgotPasswordDialog extends BaseModalDialog {

    public ForgotPasswordDialog(Frame owner, AuthService authService) {
        super(owner, "Reset Password", 360);

        JLabel desc = new JLabel("<html><body style='width:290px;'>Enter your registered email address and choose a new password.</body></html>");
        desc.setFont(Theme.regular(12.5f));
        desc.setForeground(Theme.TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField emailField = new IconInputField(VectorIcons.Type.EMAIL, "Registered Email", false);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField newPassField = new IconInputField(VectorIcons.Type.LOCK, "New Password (min 6 chars)", true);
        newPassField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField confirmField = new IconInputField(VectorIcons.Type.LOCK, "Confirm New Password", true);
        confirmField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errorLabel = createErrorLabel();

        bodyPanel.add(desc);
        bodyPanel.add(Box.createVerticalStrut(14));
        bodyPanel.add(emailField);
        bodyPanel.add(Box.createVerticalStrut(10));
        bodyPanel.add(newPassField);
        bodyPanel.add(Box.createVerticalStrut(10));
        bodyPanel.add(confirmField);
        bodyPanel.add(Box.createVerticalStrut(8));
        bodyPanel.add(errorLabel);
        bodyPanel.add(Box.createVerticalStrut(12));

        JPanel btnGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        btnGrid.setOpaque(false);
        btnGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        PrimaryButton cancelBtn = new PrimaryButton("Cancel", PrimaryButton.Style.SECONDARY_DARK);
        cancelBtn.setPreferredSize(new Dimension(130, 42));
        cancelBtn.addActionListener(e -> dispose());

        PrimaryButton resetBtn = new PrimaryButton("Reset", PrimaryButton.Style.PRIMARY_PEACH);
        resetBtn.setPreferredSize(new Dimension(130, 42));
        resetBtn.addActionListener(e -> {
            resetBtn.setEnabled(false);
            errorLabel.setText(" ");
            String email = emailField.getText();
            String pass = newPassField.getText();
            String conf = confirmField.getText();

            new SwingWorker<ServiceResult<Void>, Void>() {
                @Override
                protected ServiceResult<Void> doInBackground() {
                    return authService.resetPasswordByEmail(email, pass, conf);
                }

                @Override
                protected void done() {
                    resetBtn.setEnabled(true);
                    try {
                        ServiceResult<Void> res = get();
                        if (res.success()) {
                            dispose();
                            BaseModalDialog.showMessage(owner, "Password Updated", res.message(), false);
                        } else {
                            errorLabel.setText(res.message());
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Unable to reset password.");
                    }
                }
            }.execute();
        });

        btnGrid.add(cancelBtn);
        btnGrid.add(resetBtn);
        bodyPanel.add(btnGrid);

        finishLayout(360);
    }
}
