package com.womensafety.ui.dialogs;

import com.womensafety.models.EmergencyContact;
import com.womensafety.models.SOSRecord;
import com.womensafety.ui.Theme;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.utils.DateTimeUtils;

import javax.swing.Box;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

/**
 * Displays ONLY real database-derived safety notifications (no fake alerts).
 */
public class NotificationsDialog extends BaseModalDialog {

    public NotificationsDialog(Frame owner, List<SOSRecord> recentSos, List<EmergencyContact> contacts) {
        super(owner, "Notifications", 350);

        boolean hasItems = false;

        if (contacts == null || contacts.isEmpty()) {
            addNotice("Action Required", "No trusted contacts saved yet. Add at least one contact for SOS alerts.");
            hasItems = true;
        }

        if (recentSos != null && !recentSos.isEmpty()) {
            int count = Math.min(3, recentSos.size());
            for (int i = 0; i < count; i++) {
                SOSRecord rec = recentSos.get(i);
                addNotice(
                        rec.getDisplayTitle() + " (" + rec.getStatus() + ")",
                        "Recorded on " + DateTimeUtils.formatForDisplay(rec.getCreatedAt())
                );
                hasItems = true;
            }
        }

        if (!hasItems) {
            JLabel empty = new JLabel("No new notifications.");
            empty.setFont(Theme.regular(13f));
            empty.setForeground(Theme.TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            bodyPanel.add(empty);
            bodyPanel.add(Box.createVerticalStrut(16));
        }

        PrimaryButton closeBtn = new PrimaryButton("Close", PrimaryButton.Style.PRIMARY_PEACH);
        closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeBtn.setPreferredSize(new Dimension(310, 40));
        closeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        closeBtn.addActionListener(e -> dispose());
        bodyPanel.add(closeBtn);

        finishLayout(350);
    }

    private void addNotice(String title, String body) {
        JLabel t = new JLabel(title);
        t.setFont(Theme.bold(13f));
        t.setForeground(Theme.PEACH_PRIMARY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel b = new JLabel("<html><body style='width:280px;'>" + escapeHtml(body) + "</body></html>");
        b.setFont(Theme.regular(12f));
        b.setForeground(Theme.TEXT_SECONDARY);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);

        bodyPanel.add(t);
        bodyPanel.add(Box.createVerticalStrut(2));
        bodyPanel.add(b);
        bodyPanel.add(Box.createVerticalStrut(12));
    }
}
