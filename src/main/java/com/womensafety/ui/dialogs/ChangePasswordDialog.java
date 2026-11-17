package com.womensafety.ui.dialogs;

import com.womensafety.services.ServiceResult;
import com.womensafety.services.UserService;
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
 * Dialog for changing the authenticated user's password securely via PBKDF2.
 */
public class ChangePasswordDialog extends BaseModalDialog {

    private boolean updated = false;

    public ChangePasswordDialog(Frame owner, long userId, UserService userService) {
        super(owner, "Change Password", 360);

        IconInputField currentField = new IconInputField(VectorIcons.Type.LOCK, "Current Password", true);
        currentField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField newField = new IconInputField(VectorIcons.Type.LOCK, "New Password (min 6 chars)", true);
        newField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField confirmField = new IconInputField(VectorIcons.Type.LOCK, "Confirm New Password", true);
        confirmField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errorLabel = createErrorLabel();

        bodyPanel.add(createFieldLabel("Current Password"));
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(currentField);
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createFieldLabel("New Password"));
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(newField);
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createFieldLabel("Confirm New Password"));
        bodyPanel.add(Box.createVerticalStrut(6));
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

        PrimaryButton saveBtn = new PrimaryButton("Update", PrimaryButton.Style.PRIMARY_PEACH);
        saveBtn.setPreferredSize(new Dimension(130, 42));
        saveBtn.addActionListener(e -> {
            saveBtn.setEnabled(false);
            errorLabel.setText(" ");
            String current = currentField.getText();
            String next = newField.getText();
            String confirm = confirmField.getText();

            new SwingWorker<ServiceResult<Void>, Void>() {
                @Override
                protected ServiceResult<Void> doInBackground() {
                    return userService.changePassword(userId, current, next, confirm);
                }

                @Override
                protected void done() {
                    saveBtn.setEnabled(true);
                    try {
                        ServiceResult<Void> res = get();
                        if (res.success()) {
                            updated = true;
                            dispose();
                        } else {
                            errorLabel.setText(res.message());
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Error updating password.");
                    }
                }
            }.execute();
        });

        btnGrid.add(cancelBtn);
        btnGrid.add(saveBtn);
        bodyPanel.add(btnGrid);

        finishLayout(360);
    }

    public boolean isUpdated() {
        return updated;
    }
}
