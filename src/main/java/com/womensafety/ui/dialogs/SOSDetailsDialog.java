package com.womensafety.ui.dialogs;

import com.womensafety.models.SOSRecord;
import com.womensafety.ui.Theme;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.utils.DateTimeUtils;
import com.womensafety.utils.MapUtils;

import javax.swing.Box;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

/**
 * Displays complete database record details for an SOS History event.
 */
public class SOSDetailsDialog extends BaseModalDialog {

    public SOSDetailsDialog(Frame owner, SOSRecord record) {
        super(owner, record.getDisplayTitle(), 360);

        addDetailRow("Record ID", "#" + record.getId());
        addDetailRow("Status", record.getStatus());
        addDetailRow("Triggered At", DateTimeUtils.formatForDisplay(record.getCreatedAt()));

        if (record.getCancelledAt() != null && !record.getCancelledAt().isBlank()) {
            addDetailRow("Cancelled At", DateTimeUtils.formatForDisplay(record.getCancelledAt()));
        }

        addDetailRow("Location Mode", record.getLocationType());

        boolean hasCoords = record.getLatitude() != null && record.getLongitude() != null;
        if (hasCoords) {
            addDetailRow("Coordinates", MapUtils.formatCoordinates(record.getLatitude(), record.getLongitude()));
            bodyPanel.add(Box.createVerticalStrut(12));

            PrimaryButton mapsBtn = new PrimaryButton("Open in Google Maps", PrimaryButton.Style.PRIMARY_PEACH);
            mapsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            mapsBtn.setPreferredSize(new Dimension(320, 42));
            mapsBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            mapsBtn.addActionListener(e -> {
                String url = MapUtils.buildGoogleMapsUrl(record.getLatitude(), record.getLongitude());
                MapUtils.ActionResult openRes = MapUtils.openInBrowser(url);
                if (!openRes.success()) {
                    MapUtils.copyToClipboard(url);
                    BaseModalDialog.showMessage(owner, "Google Maps URL",
                            openRes.message() + "\n\nCopied link instead:\n" + url, false);
                }
            });
            bodyPanel.add(mapsBtn);
            bodyPanel.add(Box.createVerticalStrut(8));
        } else {
            addDetailRow("Coordinates", "Unavailable");
            bodyPanel.add(Box.createVerticalStrut(10));
        }

        PrimaryButton closeBtn = new PrimaryButton("Close", PrimaryButton.Style.SECONDARY_DARK);
        closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeBtn.setPreferredSize(new Dimension(320, 42));
        closeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        closeBtn.addActionListener(e -> dispose());
        bodyPanel.add(closeBtn);

        finishLayout(360);
    }

    private void addDetailRow(String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.regular(11.5f));
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(Theme.bold(13.5f));
        val.setForeground(Theme.TEXT_PRIMARY);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        bodyPanel.add(lbl);
        bodyPanel.add(Box.createVerticalStrut(2));
        bodyPanel.add(val);
        bodyPanel.add(Box.createVerticalStrut(10));
    }
}
