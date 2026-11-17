package com.womensafety.ui.dialogs;

import com.womensafety.config.AppConfig;
import com.womensafety.models.UserSettings;
import com.womensafety.services.ServiceResult;
import com.womensafety.services.UserService;
import com.womensafety.ui.Theme;
import com.womensafety.ui.components.PrimaryButton;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;

/**
 * Dialog for managing persistent user_settings preferences in SQLite.
 */
public class AppSettingsDialog extends BaseModalDialog {

    public AppSettingsDialog(Frame owner, UserSettings settings, UserService userService) {
        super(owner, "App Settings", 360);

        JCheckBox notifCheck = new JCheckBox("Enable SOS notification dispatch (when configured)");
        styleCheckBox(notifCheck, settings.isNotificationEnabled());

        JCheckBox locationCheck = new JCheckBox("Include location coordinates in SOS alerts");
        styleCheckBox(locationCheck, settings.isLocationSharingEnabled());

        String gatewayStatus = AppConfig.isSmsGatewayConfigured()
                ? "SMS Webhook: Configured via WSS_SMS_WEBHOOK_URL"
                : "SMS/Email Gateway: Not configured (alerts saved locally)";
        JLabel gatewayInfo = new JLabel("<html><body style='width:290px;'>" + gatewayStatus + "</body></html>");
        gatewayInfo.setFont(Theme.regular(12f));
        gatewayInfo.setForeground(Theme.TEXT_MUTED);
        gatewayInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errorLabel = createErrorLabel();

        bodyPanel.add(notifCheck);
        bodyPanel.add(Box.createVerticalStrut(10));
        bodyPanel.add(locationCheck);
        bodyPanel.add(Box.createVerticalStrut(14));
        bodyPanel.add(gatewayInfo);
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

        PrimaryButton saveBtn = new PrimaryButton("Save Settings", PrimaryButton.Style.PRIMARY_PEACH);
        saveBtn.setPreferredSize(new Dimension(130, 42));
        saveBtn.addActionListener(e -> {
            saveBtn.setEnabled(false);
            settings.setNotificationEnabled(notifCheck.isSelected());
            settings.setLocationSharingEnabled(locationCheck.isSelected());

            new SwingWorker<ServiceResult<UserSettings>, Void>() {
                @Override
                protected ServiceResult<UserSettings> doInBackground() {
                    return userService.saveSettings(settings);
                }

                @Override
                protected void done() {
                    saveBtn.setEnabled(true);
                    try {
                        ServiceResult<UserSettings> res = get();
                        if (res.success()) {
                            dispose();
                        } else {
                            errorLabel.setText(res.message());
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Failed to save settings.");
                    }
                }
            }.execute();
        });

        btnGrid.add(cancelBtn);
        btnGrid.add(saveBtn);
        bodyPanel.add(btnGrid);

        finishLayout(360);
    }

    private void styleCheckBox(JCheckBox checkBox, boolean selected) {
        checkBox.setSelected(selected);
        checkBox.setOpaque(false);
        checkBox.setForeground(Theme.TEXT_PRIMARY);
        checkBox.setFont(Theme.regular(13f));
        checkBox.setFocusPainted(false);
        checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
