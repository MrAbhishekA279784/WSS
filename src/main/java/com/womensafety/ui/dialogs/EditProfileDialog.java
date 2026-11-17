package com.womensafety.ui.dialogs;

import com.womensafety.models.User;
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
 * Dialog to edit the logged-in user's Full Name and Email in SQLite.
 */
public class EditProfileDialog extends BaseModalDialog {

    private boolean updated = false;

    public EditProfileDialog(Frame owner, User user, UserService userService) {
        super(owner, "Edit Profile", 360);

        IconInputField nameField = new IconInputField(VectorIcons.Type.PROFILE_USER, "Full Name", false);
        nameField.setText(user.getFullName());
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField emailField = new IconInputField(VectorIcons.Type.EMAIL, "Email Address", false);
        emailField.setText(user.getEmail());
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errorLabel = createErrorLabel();

        bodyPanel.add(createFieldLabel("Full Name"));
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(nameField);
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createFieldLabel("Email"));
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(emailField);
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

        PrimaryButton saveBtn = new PrimaryButton("Save", PrimaryButton.Style.PRIMARY_PEACH);
        saveBtn.setPreferredSize(new Dimension(130, 42));
        saveBtn.addActionListener(e -> {
            saveBtn.setEnabled(false);
            errorLabel.setText(" ");
            String name = nameField.getText();
            String email = emailField.getText();

            new SwingWorker<ServiceResult<User>, Void>() {
                @Override
                protected ServiceResult<User> doInBackground() {
                    return userService.updateProfile(user.getId(), name, email);
                }

                @Override
                protected void done() {
                    saveBtn.setEnabled(true);
                    try {
                        ServiceResult<User> res = get();
                        if (res.success()) {
                            updated = true;
                            dispose();
                        } else {
                            errorLabel.setText(res.message());
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Unable to update profile.");
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
