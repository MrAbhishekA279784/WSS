package com.womensafety.ui;

import com.womensafety.config.AppConfig;
import com.womensafety.models.LocationRecord;
import com.womensafety.models.User;
import com.womensafety.services.LocationService;
import com.womensafety.services.ServiceResult;
import com.womensafety.services.SessionManager;
import com.womensafety.ui.components.IconInputField;
import com.womensafety.ui.components.NavigationBar;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.ui.components.RoundedPanel;
import com.womensafety.ui.components.ScreenHeader;
import com.womensafety.ui.components.VectorIcons;
import com.womensafety.ui.dialogs.BaseModalDialog;
import com.womensafety.utils.MapUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Screen 05 — Live Location Screen matching reference design:
 * dark vector map canvas (honestly labelled simulated GPS mode), Current Location card,
 * Share Location peach button (copies Google Maps URL + opens browser), and NavigationBar.
 */
public class LocationScreen extends JPanel {

    private final LocationService locationService;
    private final JLabel coordsLabel;
    private final JLabel addressLabel;
    private final JLabel modeBadgeLabel;
    private final SimulatedMapCanvas mapCanvas;
    private LocationRecord currentLocation;

    public LocationScreen(LocationService locationService,
                          Runnable onBack,
                          Consumer<NavigationBar.Tab> onNavigateTab) {
        this.locationService = locationService;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(new ScreenHeader("Live Location", onBack), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Dark Map Area with center marker, crosshair button, and honest simulated indicator
        mapCanvas = new SimulatedMapCanvas();
        mapCanvas.setLayout(new BorderLayout());
        mapCanvas.setPreferredSize(new Dimension(420, 520));
        mapCanvas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 540));

        JPanel topOverlay = new JPanel(new BorderLayout());
        topOverlay.setOpaque(false);
        topOverlay.setBorder(BorderFactory.createEmptyBorder(10, 14, 0, 14));
        modeBadgeLabel = new JLabel("Simulated Location Mode");
        modeBadgeLabel.setFont(Theme.regular(11f));
        modeBadgeLabel.setForeground(Theme.TEXT_SECONDARY);
        modeBadgeLabel.setOpaque(true);
        modeBadgeLabel.setBackground(new Color(0x14, 0x14, 0x14, 210));
        modeBadgeLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        topOverlay.add(modeBadgeLabel, BorderLayout.EAST);
        mapCanvas.add(topOverlay, BorderLayout.NORTH);

        JPanel bottomRightOverlay = new JPanel(new BorderLayout());
        bottomRightOverlay.setOpaque(false);
        bottomRightOverlay.setBorder(BorderFactory.createEmptyBorder(0, 0, 74, 20));

        JButton targetBtn = new JButton(VectorIcons.get(VectorIcons.Type.CROSSHAIR, 20, Theme.TEXT_PRIMARY)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x14, 0x14, 0x14));
                g2.fill(new Ellipse2D.Double(1, 1, getWidth() - 2, getHeight() - 2));
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new Ellipse2D.Double(1, 1, getWidth() - 2, getHeight() - 2));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        targetBtn.setPreferredSize(new Dimension(42, 42));
        targetBtn.setBorderPainted(false);
        targetBtn.setContentAreaFilled(false);
        targetBtn.setFocusPainted(false);
        targetBtn.setOpaque(false);
        targetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        targetBtn.setToolTipText("Set or update simulated coordinates");
        targetBtn.addActionListener(e -> openCoordinateEditor());

        bottomRightOverlay.add(targetBtn, BorderLayout.EAST);
        mapCanvas.add(bottomRightOverlay, BorderLayout.SOUTH);

        // Current Location Card overlapping bottom edge of map matching reference Screen 05
        JPanel paddedBottom = new JPanel();
        paddedBottom.setOpaque(false);
        paddedBottom.setLayout(new BoxLayout(paddedBottom, BoxLayout.Y_AXIS));
        paddedBottom.setBorder(BorderFactory.createEmptyBorder(0, 20, 4, 20));

        RoundedPanel locationCard = new RoundedPanel(new BorderLayout(12, 0), Theme.CARD_RADIUS, Theme.BG_CARD, Theme.BORDER_SUBTLE);
        locationCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        locationCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JLabel pinLabel = new JLabel(VectorIcons.get(VectorIcons.Type.LOCATION_PIN, 22, Theme.PEACH_PRIMARY));
        pinLabel.setVerticalAlignment(SwingConstants.TOP);

        JPanel locText = new JPanel();
        locText.setOpaque(false);
        locText.setLayout(new BoxLayout(locText, BoxLayout.Y_AXIS));

        JLabel locHeading = new JLabel("Current Location");
        locHeading.setFont(Theme.bold(14f));
        locHeading.setForeground(Theme.TEXT_PRIMARY);

        coordsLabel = new JLabel(MapUtils.formatCoordinates(AppConfig.DEFAULT_SIMULATED_LAT, AppConfig.DEFAULT_SIMULATED_LNG));
        coordsLabel.setFont(Theme.regular(13f));
        coordsLabel.setForeground(Theme.TEXT_PRIMARY);

        addressLabel = new JLabel(AppConfig.DEFAULT_SIMULATED_ADDRESS);
        addressLabel.setFont(Theme.regular(11.5f));
        addressLabel.setForeground(Theme.TEXT_SECONDARY);

        locText.add(locHeading);
        locText.add(Box.createVerticalStrut(4));
        locText.add(coordsLabel);
        locText.add(Box.createVerticalStrut(3));
        locText.add(addressLabel);

        locationCard.add(pinLabel, BorderLayout.WEST);
        locationCard.add(locText, BorderLayout.CENTER);
        locationCard.enableHover(this::openCurrentInGoogleMaps);

        PrimaryButton shareBtn = new PrimaryButton(
                "Share Location",
                PrimaryButton.Style.PRIMARY_PEACH,
                VectorIcons.get(VectorIcons.Type.SHARE, 17, Theme.TEXT_ON_PEACH)
        );
        shareBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        shareBtn.addActionListener(e -> handleShareLocation());

        paddedBottom.add(locationCard);
        paddedBottom.add(Box.createVerticalStrut(14));
        paddedBottom.add(shareBtn);

        JPanel overlapStage = new JPanel();
        overlapStage.setOpaque(false);
        overlapStage.setLayout(new javax.swing.OverlayLayout(overlapStage));

        JPanel mapLayer = new JPanel(new BorderLayout());
        mapLayer.setOpaque(false);
        mapLayer.add(mapCanvas, BorderLayout.NORTH);

        JPanel cardLayer = new JPanel(new BorderLayout());
        cardLayer.setOpaque(false);
        cardLayer.add(paddedBottom, BorderLayout.SOUTH);

        overlapStage.add(cardLayer);
        overlapStage.add(mapLayer);

        body.add(overlapStage);

        add(body, BorderLayout.CENTER);
        add(new NavigationBar(NavigationBar.Tab.LOCATION, onNavigateTab), BorderLayout.SOUTH);
    }

    public void refreshData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        new SwingWorker<ServiceResult<LocationRecord>, Void>() {
            @Override
            protected ServiceResult<LocationRecord> doInBackground() {
                return locationService.getCurrentLocation(user.getId());
            }

            @Override
            protected void done() {
                try {
                    ServiceResult<LocationRecord> res = get();
                    if (res.success() && res.data() != null) {
                        updateDisplay(res.data());
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void updateDisplay(LocationRecord rec) {
        this.currentLocation = rec;
        coordsLabel.setText(MapUtils.formatCoordinates(rec.getLatitude(), rec.getLongitude()));
        addressLabel.setText(rec.getAddress() != null && !rec.getAddress().isBlank()
                ? rec.getAddress()
                : "Simulated Location (" + rec.getLocationType() + ")");
        modeBadgeLabel.setText("Location: " + rec.getLocationType());
        mapCanvas.repaint();
    }

    private void handleShareLocation() {
        User user = SessionManager.getCurrentUser();
        if (user == null || currentLocation == null) {
            return;
        }
        String url = locationService.getShareableGoogleMapsUrl(currentLocation);
        MapUtils.ActionResult copyRes = MapUtils.copyToClipboard(url);

        // Save snapshot in SQLite location_history
        LocationRecord snapshot = currentLocation;
        new SwingWorker<ServiceResult<LocationRecord>, Void>() {
            @Override
            protected ServiceResult<LocationRecord> doInBackground() {
                return locationService.refreshAndRecordLocation(
                        user.getId(),
                        snapshot.getLatitude(),
                        snapshot.getLongitude(),
                        snapshot.getAddress(),
                        snapshot.getLocationType()
                );
            }
        }.execute();

        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        boolean openNow = BaseModalDialog.showConfirm(
                owner,
                "Location Link Ready",
                (copyRes.success() ? "Google Maps link copied to clipboard:\n" : "Shareable Google Maps link:\n")
                        + url + "\n\nOpen this location in Google Maps now?",
                "Open Maps",
                false
        );
        if (openNow) {
            MapUtils.ActionResult openRes = MapUtils.openInBrowser(url);
            if (!openRes.success()) {
                BaseModalDialog.showMessage(owner, "Browser Unavailable", openRes.message(), true);
            }
        }
    }

    private void openCurrentInGoogleMaps() {
        if (currentLocation == null) {
            return;
        }
        String url = locationService.getShareableGoogleMapsUrl(currentLocation);
        MapUtils.ActionResult openRes = MapUtils.openInBrowser(url);
        if (!openRes.success()) {
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
            MapUtils.copyToClipboard(url);
            BaseModalDialog.showMessage(owner, "Google Maps Link",
                    openRes.message() + "\n\nLink copied to clipboard:\n" + url, false);
        }
    }

    private void openCoordinateEditor() {
        User user = SessionManager.getCurrentUser();
        if (user == null || currentLocation == null) {
            return;
        }
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        BaseModalDialog dialog = new BaseModalDialog(owner, "Update Simulated Location", 360);

        JLabel note = new JLabel("<html><body style='width:290px;'>Desktop hardware GPS is unavailable. Enter coordinates to simulate and save to SQLite.</body></html>");
        note.setFont(Theme.regular(12f));
        note.setForeground(Theme.TEXT_SECONDARY);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField latField = new IconInputField(VectorIcons.Type.LOCATION_PIN, "Latitude (-90 to 90)", false);
        latField.setText(String.valueOf(currentLocation.getLatitude()));
        latField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField lngField = new IconInputField(VectorIcons.Type.LOCATION_PIN, "Longitude (-180 to 180)", false);
        lngField.setText(String.valueOf(currentLocation.getLongitude()));
        lngField.setAlignmentX(Component.LEFT_ALIGNMENT);

        IconInputField addrField = new IconInputField(VectorIcons.Type.HOME, "Location Label", false);
        addrField.setText(currentLocation.getAddress() != null ? currentLocation.getAddress() : "");
        addrField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(Theme.regular(12f));
        errLabel.setForeground(Theme.EMERGENCY);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        dialog.getRootPane();
        java.awt.Container body = (java.awt.Container) dialog.getContentPane().getComponent(1);
        body.add(note);
        body.add(Box.createVerticalStrut(12));
        body.add(latField);
        body.add(Box.createVerticalStrut(10));
        body.add(lngField);
        body.add(Box.createVerticalStrut(10));
        body.add(addrField);
        body.add(Box.createVerticalStrut(8));
        body.add(errLabel);
        body.add(Box.createVerticalStrut(10));

        JPanel btnGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        btnGrid.setOpaque(false);
        btnGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        PrimaryButton cancelBtn = new PrimaryButton("Cancel", PrimaryButton.Style.SECONDARY_DARK);
        cancelBtn.setPreferredSize(new Dimension(130, 42));
        cancelBtn.addActionListener(ev -> dialog.dispose());

        PrimaryButton saveBtn = new PrimaryButton("Update", PrimaryButton.Style.PRIMARY_PEACH);
        saveBtn.setPreferredSize(new Dimension(130, 42));
        saveBtn.addActionListener(ev -> {
            try {
                double lat = Double.parseDouble(latField.getText().trim());
                double lng = Double.parseDouble(lngField.getText().trim());
                String addr = addrField.getText().trim();
                saveBtn.setEnabled(false);

                new SwingWorker<ServiceResult<LocationRecord>, Void>() {
                    @Override
                    protected ServiceResult<LocationRecord> doInBackground() {
                        return locationService.refreshAndRecordLocation(
                                user.getId(), lat, lng, addr, LocationRecord.TYPE_MANUAL);
                    }

                    @Override
                    protected void done() {
                        saveBtn.setEnabled(true);
                        try {
                            ServiceResult<LocationRecord> res = get();
                            if (res.success() && res.data() != null) {
                                updateDisplay(res.data());
                                dialog.dispose();
                            } else {
                                errLabel.setText(res.message());
                            }
                        } catch (Exception ex) {
                            errLabel.setText("Failed to update coordinates.");
                        }
                    }
                }.execute();
            } catch (NumberFormatException ex) {
                errLabel.setText("Please enter valid numeric latitude and longitude.");
            }
        });

        btnGrid.add(cancelBtn);
        btnGrid.add(saveBtn);
        body.add(btnGrid);

        dialog.pack();
        dialog.setSize(360, Math.max(dialog.getPreferredSize().height, 360));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /**
     * Custom-painted dark street grid placeholder clearly labelled as simulated mode.
     */
    private static class SimulatedMapCanvas extends JPanel {
        SimulatedMapCanvas() {
            setBackground(new Color(0x14, 0x14, 0x14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Subtle dark city block fills scaled to canvas height
            g2.setColor(new Color(0x1B, 0x1B, 0x1B));
            g2.fill(new RoundRectangle2D.Double(18, h * 0.05, 95, h * 0.16, 10, 10));
            g2.fill(new RoundRectangle2D.Double(138, h * 0.03, 110, h * 0.18, 10, 10));
            g2.fill(new RoundRectangle2D.Double(272, h * 0.06, 120, h * 0.17, 10, 10));
            g2.fill(new RoundRectangle2D.Double(24, h * 0.28, 100, h * 0.20, 10, 10));
            g2.fill(new RoundRectangle2D.Double(266, h * 0.30, 118, h * 0.19, 10, 10));
            g2.fill(new RoundRectangle2D.Double(70, h * 0.56, 120, h * 0.16, 10, 10));
            g2.fill(new RoundRectangle2D.Double(235, h * 0.58, 130, h * 0.15, 10, 10));
            g2.fill(new RoundRectangle2D.Double(28, h * 0.77, 110, h * 0.14, 10, 10));
            g2.fill(new RoundRectangle2D.Double(260, h * 0.78, 125, h * 0.14, 10, 10));

            // Arterial dark road curves matching Screen 05
            g2.setColor(new Color(0x2D, 0x2D, 0x2D));
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            GeneralPath road1 = new GeneralPath();
            road1.moveTo(-10, h * 0.24);
            road1.curveTo(w * 0.30, h * 0.33, w * 0.65, h * 0.14, w + 10, h * 0.22);
            g2.draw(road1);

            GeneralPath road2 = new GeneralPath();
            road2.moveTo(w * 0.38, -10);
            road2.curveTo(w * 0.42, h * 0.35, w * 0.34, h * 0.70, w * 0.50, h + 10);
            g2.draw(road2);

            GeneralPath road3 = new GeneralPath();
            road3.moveTo(-10, h * 0.72);
            road3.curveTo(w * 0.35, h * 0.64, w * 0.68, h * 0.80, w + 10, h * 0.56);
            g2.draw(road3);

            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(0x24, 0x24, 0x24));
            g2.drawLine((int) (w * 0.16), 0, (int) (w * 0.24), h);
            g2.drawLine((int) (w * 0.76), 0, (int) (w * 0.66), h);

            // Center peach location dot with soft translucent radar rings
            double cx = w / 2.0;
            double cy = h * 0.44;

            RadialGradientPaint radar = new RadialGradientPaint(
                    new Point2D.Double(cx, cy),
                    48f,
                    new float[]{0f, 0.55f, 1f},
                    new Color[]{
                            new Color(0xF5, 0xB6, 0xA9, 85),
                            new Color(0xE8, 0xA9, 0x9D, 35),
                            new Color(0xE8, 0xA9, 0x9D, 0)
                    }
            );
            g2.setPaint(radar);
            g2.fill(new Ellipse2D.Double(cx - 48, cy - 48, 96, 96));

            g2.setColor(new Color(0xEC, 0xA9, 0x9C, 80));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new Ellipse2D.Double(cx - 24, cy - 24, 48, 48));

            g2.setColor(Theme.PEACH_PRIMARY);
            g2.fill(new Ellipse2D.Double(cx - 8, cy - 8, 16, 16));

            g2.dispose();
        }
    }
}
