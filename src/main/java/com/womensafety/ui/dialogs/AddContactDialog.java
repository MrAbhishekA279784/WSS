package com.womensafety.ui.dialogs;

import com.womensafety.models.EmergencyContact;
import com.womensafety.services.ContactService;
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
 * Clean dialog to add a new trusted emergency contact to SQLite.
 */
public class AddContactDialog extends BaseModalDialog {

    private boolean saved = false;

    public AddContactDialog(Frame owner, long userId, ContactService contactService) {
        super(owner, "Add Trusted Contact", 360);

        IconInputField nameField = new IconInputField(VectorIcons.Type.PROFILE_USER, "Contact Name (e.g. Mom)", false);
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField phoneField = new IconInputField(VectorIcons.Type.PHONE, "Phone Number (e.g. +91 98765 43210)", false);
        phoneField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField relationField = new IconInputField(VectorIcons.Type.CONTACTS, "Relationship (optional)", false);
        relationField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errorLabel = createErrorLabel();

        bodyPanel.add(createFieldLabel("Full Name"));
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(nameField);
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createFieldLabel("Phone Number"));
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(phoneField);
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createFieldLabel("Relationship"));
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(relationField);
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

        PrimaryButton saveBtn = new PrimaryButton("Save Contact", PrimaryButton.Style.PRIMARY_PEACH);
        saveBtn.setPreferredSize(new Dimension(130, 42));
        saveBtn.addActionListener(e -> {
            saveBtn.setEnabled(false);
            errorLabel.setText(" ");
            String name = nameField.getText();
            String phone = phoneField.getText();
            String rel = relationField.getText();

            new SwingWorker<ServiceResult<EmergencyContact>, Void>() {
                @Override
                protected ServiceResult<EmergencyContact> doInBackground() {
                    return contactService.addContact(userId, name, phone, rel);
                }

                @Override
                protected void done() {
                    saveBtn.setEnabled(true);
                    try {
                        ServiceResult<EmergencyContact> res = get();
                        if (res.success()) {
                            saved = true;
                            dispose();
                        } else {
                            errorLabel.setText(res.message());
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Failed to save contact.");
                    }
                }
            }.execute();
        });

        btnGrid.add(cancelBtn);
        btnGrid.add(saveBtn);
        bodyPanel.add(btnGrid);

        finishLayout(360);
    }

    public boolean isSaved() {
        return saved;
    }
}
