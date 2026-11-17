package com.womensafety.ui;

import com.womensafety.models.LocationRecord;
import com.womensafety.models.SOSRecord;
import com.womensafety.models.User;
import com.womensafety.models.UserSettings;
import com.womensafety.services.NotificationService;
import com.womensafety.services.SOSService;
import com.womensafety.services.ServiceResult;
import com.womensafety.services.SessionManager;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.ui.components.ScreenHeader;
import com.womensafety.ui.components.VectorIcons;
import com.womensafety.ui.dialogs.BaseModalDialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

/**
 * Screen 04 — SOS Activation Screen matching reference layout:
 * "Sending SOS", concentric radar rings with siren icon, 3 real progress rows, and Cancel button.
 */
public class SOSActivationScreen extends JPanel {

    private enum StepState {
        PENDING,
        IN_PROGRESS,
        SUCCESS,
        WARNING_OR_NOT_CONFIGURED,
        FAILED
    }

    private final SOSService sosService;
    private final Runnable onFinishedOrCancelled;

    private final StepRow locationStep;
    private final StepRow notifyStep;
    private final StepRow saveStep;
    private final JLabel honestSummaryLabel;
    private final PrimaryButton cancelButton;
    private final ConcentricSirenPanel sirenPanel;

    private volatile SOSRecord activeRecord;
    private volatile boolean cancelledByUser = false;
    private Timer spinnerTimer;
    private float spinAngle = 0f;

    public SOSActivationScreen(SOSService sosService, Runnable onFinishedOrCancelled) {
        this.sosService = sosService;
        this.onFinishedOrCancelled = onFinishedOrCancelled;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(new ScreenHeader("", this::handleCancelAction), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(8, 28, 28, 28));

        JLabel title = new JLabel("Sending SOS", SwingConstants.CENTER);
        title.setFont(Theme.bold(22f));
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        sirenPanel = new ConcentricSirenPanel();
        sirenPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stepsBox = new JPanel();
        stepsBox.setOpaque(false);
        stepsBox.setLayout(new BoxLayout(stepsBox, BoxLayout.Y_AXIS));
        stepsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        stepsBox.setMaximumSize(new Dimension(360, 150));

        locationStep = new StepRow("Getting your location...");
        notifyStep = new StepRow("Notifying emergency contacts...");
        saveStep = new StepRow("Saving SOS record...");

        stepsBox.add(locationStep);
        stepsBox.add(Box.createVerticalStrut(12));
        stepsBox.add(notifyStep);
        stepsBox.add(Box.createVerticalStrut(12));
        stepsBox.add(saveStep);

        honestSummaryLabel = new JLabel("<html><center></center></html>", SwingConstants.CENTER);
        honestSummaryLabel.setFont(Theme.regular(12f));
        honestSummaryLabel.setForeground(Theme.TEXT_SECONDARY);
        honestSummaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        honestSummaryLabel.setMaximumSize(new Dimension(340, 64));

        cancelButton = new PrimaryButton("Cancel", PrimaryButton.Style.SECONDARY_DARK);
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> handleCancelAction());

        center.add(Box.createVerticalStrut(10));
        center.add(title);
        center.add(Box.createVerticalStrut(26));
        center.add(sirenPanel);
        center.add(Box.createVerticalStrut(28));
        center.add(stepsBox);
        center.add(Box.createVerticalStrut(16));
        center.add(honestSummaryLabel);
        center.add(Box.createVerticalGlue());
        center.add(cancelButton);

        add(center, BorderLayout.CENTER);
    }

    public void startSOSWorkflow() {
        User user = SessionManager.getCurrentUser();
        UserSettings settings = SessionManager.getCurrentSettings();
        if (user == null) {
            return;
        }

        activeRecord = null;
        cancelledByUser = false;
        cancelButton.setText("Cancel");
        cancelButton.setEnabled(true);
        honestSummaryLabel.setText(" ");

        locationStep.setState(StepState.IN_PROGRESS, "Getting your location...");
        notifyStep.setState(StepState.PENDING, "Notifying emergency contacts...");
        saveStep.setState(StepState.PENDING, "Saving SOS record...");

        if (spinnerTimer != null) {
            spinnerTimer.stop();
        }
        spinnerTimer = new Timer(40, e -> {
            spinAngle = (spinAngle + 10f) % 360f;
            sirenPanel.repaint();
            locationStep.repaint();
            notifyStep.repaint();
            saveStep.repaint();
        });
        spinnerTimer.start();

        new SwingWorker<Void, Runnable>() {
            private LocationRecord capturedLocation;
            private ServiceResult<SOSRecord> saveResult;
            private NotificationService.NotificationResult notifyResult;

            @Override
            protected Void doInBackground() throws Exception {
                boolean locEnabled = settings == null || settings.isLocationSharingEnabled();
                ServiceResult<LocationRecord> locRes = sosService.resolveLocationForSOS(user, locEnabled);
                capturedLocation = locRes.data();
                Thread.sleep(350);

                publish(() -> {
                    if (locRes.success() && capturedLocation != null) {
                        locationStep.setState(StepState.SUCCESS,
                                "Location captured (" + capturedLocation.getLocationType() + ")");
                    } else if (locRes.success()) {
                        locationStep.setState(StepState.WARNING_OR_NOT_CONFIGURED, locRes.message());
                    } else {
                        locationStep.setState(StepState.FAILED, locRes.message());
                    }
                    notifyStep.setState(StepState.IN_PROGRESS, "Notifying emergency contacts...");
                });

                if (cancelledByUser) {
                    return null;
                }

                // Always persist SOS record to SQLite even if external notification is unconfigured/fails
                notifyResult = sosService.dispatchNotifications(user, settings, capturedLocation);
                Thread.sleep(350);

                publish(() -> {
                    if (notifyResult.isDelivered()) {
                        notifyStep.setState(StepState.SUCCESS, notifyResult.message());
                    } else if (notifyResult.state() == NotificationService.DeliveryState.FAILED) {
                        notifyStep.setState(StepState.FAILED, "Notification delivery failed");
                    } else {
                        notifyStep.setState(StepState.WARNING_OR_NOT_CONFIGURED,
                                "Contact alert not configured");
                    }
                    saveStep.setState(StepState.IN_PROGRESS, "Saving SOS record...");
                });

                if (cancelledByUser) {
                    return null;
                }

                String initialStatus = (notifyResult.isDelivered())
                        ? SOSRecord.STATUS_COMPLETED
                        : SOSRecord.STATUS_ACTIVE;
                saveResult = sosService.saveSOSRecord(user, capturedLocation, initialStatus);
                if (saveResult.success()) {
                    activeRecord = saveResult.data();
                }
                Thread.sleep(250);
                return null;
            }

            @Override
            protected void process(java.util.List<Runnable> chunks) {
                for (Runnable r : chunks) {
                    r.run();
                }
            }

            @Override
            protected void done() {
                if (spinnerTimer != null) {
                    spinnerTimer.stop();
                }
                if (cancelledByUser) {
                    return;
                }

                if (saveResult != null && saveResult.success()) {
                    saveStep.setState(StepState.SUCCESS, "SOS record saved to database");
                } else {
                    saveStep.setState(StepState.FAILED,
                            saveResult != null ? saveResult.message() : "Database save failed");
                }

                if (notifyResult != null && !notifyResult.isDelivered()) {
                    honestSummaryLabel.setText("<html><center>" + notifyResult.message() + "</center></html>");
                } else if (notifyResult != null) {
                    honestSummaryLabel.setText("<html><center>" + notifyResult.message() + "</center></html>");
                }
                cancelButton.setText("Cancel SOS / Done");
            }
        }.execute();
    }

    private void handleCancelAction() {
        User user = SessionManager.getCurrentUser();
        if (activeRecord == null) {
            cancelledByUser = true;
            if (spinnerTimer != null) {
                spinnerTimer.stop();
            }
            if (onFinishedOrCancelled != null) {
                onFinishedOrCancelled.run();
            }
            return;
        }

        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        boolean confirm = BaseModalDialog.showConfirm(
                owner,
                "Cancel SOS Alert",
                "Mark this SOS alert (#" + activeRecord.getId() + ") as cancelled in your history, or keep it active and return home?",
                "Mark Cancelled",
                false
        );

        if (confirm && user != null) {
            cancelButton.setEnabled(false);
            long sosId = activeRecord.getId();
            new SwingWorker<ServiceResult<SOSRecord>, Void>() {
                @Override
                protected ServiceResult<SOSRecord> doInBackground() {
                    return sosService.cancelSOS(sosId, user.getId());
                }

                @Override
                protected void done() {
                    cancelButton.setEnabled(true);
                    activeRecord = null;
                    if (onFinishedOrCancelled != null) {
                        onFinishedOrCancelled.run();
                    }
                }
            }.execute();
        } else {
            if (onFinishedOrCancelled != null) {
                onFinishedOrCancelled.run();
            }
        }
    }

    private class ConcentricSirenPanel extends JPanel {
        ConcentricSirenPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(220, 220));
            setMaximumSize(new Dimension(220, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double cx = getWidth() / 2.0;
            double cy = getHeight() / 2.0;

            double[] radii = {104, 80, 56};
            Color[] fills = {
                    new Color(0x10, 0x10, 0x10),
                    new Color(0x16, 0x14, 0x14),
                    new Color(0x1E, 0x18, 0x18)
            };
            Color[] strokes = {
                    new Color(0x23, 0x20, 0x20),
                    new Color(0x32, 0x2B, 0x2A),
                    new Color(0x5A, 0x45, 0x42)
            };

            for (int i = 0; i < radii.length; i++) {
                double r = radii[i];
                g2.setColor(fills[i]);
                g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
                g2.setColor(strokes[i]);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
            }

            int iconSize = 48;
            g2.translate(cx - iconSize / 2.0, cy - iconSize / 2.0);
            VectorIcons.paint(g2, VectorIcons.Type.SIREN, iconSize, Theme.PEACH_PRIMARY);
            g2.dispose();
        }
    }

    private class StepRow extends JPanel {
        private StepState state = StepState.PENDING;
        private final JLabel textLabel;

        StepRow(String initialText) {
            super(new BorderLayout(12, 0));
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 4));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JPanel indicator = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int s = 20;
                    switch (state) {
                        case PENDING -> {
                            g2.setColor(Theme.BORDER_SUBTLE);
                            g2.setStroke(new BasicStroke(1.5f));
                            g2.draw(new Ellipse2D.Double(2, 2, s - 4, s - 4));
                        }
                        case IN_PROGRESS -> {
                            g2.setColor(Theme.BORDER_SUBTLE);
                            g2.setStroke(new BasicStroke(1.8f));
                            g2.draw(new Ellipse2D.Double(2, 2, s - 4, s - 4));
                            g2.setColor(Theme.PEACH_PRIMARY);
                            g2.draw(new Arc2D.Double(2, 2, s - 4, s - 4, spinAngle, 120, Arc2D.OPEN));
                        }
                        case SUCCESS -> {
                            g2.setColor(Theme.SUCCESS_BG);
                            g2.fill(new Ellipse2D.Double(1, 1, s - 2, s - 2));
                            g2.setColor(Theme.SUCCESS);
                            g2.setStroke(new BasicStroke(1.2f));
                            g2.draw(new Ellipse2D.Double(1, 1, s - 2, s - 2));
                            VectorIcons.paint(g2, VectorIcons.Type.CHECK_CIRCLE, s, Theme.SUCCESS);
                        }
                        case WARNING_OR_NOT_CONFIGURED -> {
                            g2.setColor(Theme.EMERGENCY_BG);
                            g2.fill(new Ellipse2D.Double(1, 1, s - 2, s - 2));
                            g2.setColor(Theme.PEACH_PRIMARY);
                            g2.setStroke(new BasicStroke(1.2f));
                            g2.draw(new Ellipse2D.Double(1, 1, s - 2, s - 2));
                            VectorIcons.paint(g2, VectorIcons.Type.ALERT_CIRCLE, s, Theme.PEACH_PRIMARY);
                        }
                        case FAILED -> {
                            g2.setColor(Theme.EMERGENCY_BG);
                            g2.fill(new Ellipse2D.Double(1, 1, s - 2, s - 2));
                            g2.setColor(Theme.EMERGENCY);
                            g2.setStroke(new BasicStroke(1.2f));
                            g2.draw(new Ellipse2D.Double(1, 1, s - 2, s - 2));
                            VectorIcons.paint(g2, VectorIcons.Type.ALERT_CIRCLE, s, Theme.EMERGENCY);
                        }
                    }
                    g2.dispose();
                }
            };
            indicator.setOpaque(false);
            indicator.setPreferredSize(new Dimension(22, 22));

            textLabel = new JLabel(initialText);
            textLabel.setFont(Theme.regular(13f));
            textLabel.setForeground(Theme.TEXT_PRIMARY);

            add(indicator, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
        }

        void setState(StepState state, String text) {
            this.state = state;
            this.textLabel.setText(text);
            this.textLabel.setForeground(state == StepState.PENDING ? Theme.TEXT_MUTED : Theme.TEXT_PRIMARY);
            repaint();
        }
    }
}
