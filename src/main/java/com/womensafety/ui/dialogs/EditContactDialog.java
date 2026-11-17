package com.womensafety.ui.dialogs;

import com.womensafety.models.EmergencyContact;
import com.womensafety.services.ContactService;
import com.womensafety.services.ServiceResult;
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
 * Dialog to edit or delete an existing trusted emergency contact.
 */
public class EditContactDialog extends BaseModalDialog {

    private boolean changed = false;

    public EditContactDialog(Frame owner, EmergencyContact contact, ContactService contactService) {
        super(owner, "Edit Contact", 360);

        IconInputField nameField = new IconInputField(VectorIcons.Type.PROFILE_USER, "Contact Name", false);
        nameField.setText(contact.getContactName());
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField phoneField = new IconInputField(VectorIcons.Type.PHONE, "Phone Number", false);
        phoneField.setText(contact.getPhoneNumber());
        phoneField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField relationField = new IconInputField(VectorIcons.Type.CONTACTS, "Relationship (optional)", false);
        relationField.setText(contact.getRelationship() != null ? contact.getRelationship() : "");
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

        PrimaryButton saveBtn = new PrimaryButton("Update", PrimaryButton.Style.PRIMARY_PEACH);
        saveBtn.setPreferredSize(new Dimension(130, 42));
        saveBtn.addActionListener(e -> {
            saveBtn.setEnabled(false);
            errorLabel.setText(" ");
            String name = nameField.getText();
            String phone = phoneField.getText();
            String rel = relationField.getText();

            new SwingWorker<ServiceResult<Void>, Void>() {
                @Override
                protected ServiceResult<Void> doInBackground() {
                    return contactService.updateContact(contact.getId(), contact.getUserId(), name, phone, rel);
                }

                @Override
                protected void done() {
                    saveBtn.setEnabled(true);
                    try {
                        ServiceResult<Void> res = get();
                        if (res.success()) {
                            changed = true;
                            dispose();
                        } else {
                            errorLabel.setText(res.message());
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Failed to update contact.");
                    }
                }
            }.execute();
        });

        btnGrid.add(cancelBtn);
        btnGrid.add(saveBtn);
        bodyPanel.add(btnGrid);
        bodyPanel.add(Box.createVerticalStrut(10));

        PrimaryButton deleteBtn = new PrimaryButton("Delete Contact", PrimaryButton.Style.EMERGENCY_OUTLINE);
        deleteBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteBtn.setPreferredSize(new Dimension(320, 42));
        deleteBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        deleteBtn.addActionListener(e -> {
            boolean confirmDelete = BaseModalDialog.showConfirm(
                    owner,
                    "Delete Contact",
                    "Remove " + contact.getContactName() + " from your trusted contacts?",
                    "Delete",
                    true
            );
            if (!confirmDelete) {
                return;
            }
            deleteBtn.setEnabled(false);
            new SwingWorker<ServiceResult<Void>, Void>() {
                @Override
                protected ServiceResult<Void> doInBackground() {
                    return contactService.deleteContact(contact.getId(), contact.getUserId());
                }

                @Override
                protected void done() {
                    deleteBtn.setEnabled(true);
                    try {
                        ServiceResult<Void> res = get();
                        if (res.success()) {
                            changed = true;
                            dispose();
                        } else {
                            errorLabel.setText(res.message());
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Failed to delete contact.");
                    }
                }
            }.execute();
        });
        bodyPanel.add(deleteBtn);

        finishLayout(360);
    }

    public boolean isChanged() {
        return changed;
    }
}
