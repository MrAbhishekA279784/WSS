package com.womensafety.ui;

import com.womensafety.config.AppConfig;
import com.womensafety.database.DatabaseInitializer;
import com.womensafety.database.DatabaseManager;
import com.womensafety.services.SessionManager;
import com.womensafety.ui.components.VectorIcons;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Screen 01 — Splash Screen matching reference design:
 * pure black canvas, subtle corner arcs, peach shield emblem, title, subtitle, and bottom progress bar.
 */
public class SplashScreen extends JPanel {

    private float progress = 0.08f;
    private Timer animTimer;
    private boolean initComplete = false;
    private String initError = null;
    private final Runnable onNavigateNext;

    public SplashScreen(Runnable onNavigateNext) {
        this.onNavigateNext = onNavigateNext;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        JPanel centerBox = new JPanel();
        centerBox.setOpaque(false);
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));

        JLabel shieldLogo = new JLabel(VectorIcons.get(VectorIcons.Type.SHIELD_WOMAN, 76, Theme.PEACH_PRIMARY));
        shieldLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appTitle = new JLabel(AppConfig.APP_NAME, SwingConstants.CENTER);
        appTitle.setFont(Theme.bold(26f));
        appTitle.setForeground(Theme.TEXT_PRIMARY);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(AppConfig.APP_SUBTITLE, SwingConstants.CENTER);
        subtitle.setFont(Theme.regular(13f));
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerBox.add(Box.createVerticalGlue());
        centerBox.add(shieldLogo);
        centerBox.add(Box.createVerticalStrut(14));
        centerBox.add(appTitle);
        centerBox.add(Box.createVerticalStrut(6));
        centerBox.add(subtitle);
        centerBox.add(Box.createVerticalGlue());

        add(centerBox, BorderLayout.CENTER);

        // Minimal bottom loading bar matching reference
        JPanel bottomBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int barW = 136;
                int barH = 4;
                int x = (getWidth() - barW) / 2;
                int y = 14;

                g2.setColor(Theme.BG_ELEVATED);
                g2.fill(new RoundRectangle2D.Double(x, y, barW, barH, barH, barH));

                g2.setColor(Theme.PEACH_PRIMARY);
                double fillW = Math.max(24, barW * progress);
                g2.fill(new RoundRectangle2D.Double(x, y, fillW, barH, barH, barH));

                g2.dispose();
            }
        };
        bottomBar.setOpaque(false);
        bottomBar.setPreferredSize(new Dimension(420, 62));
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void startInitialization() {
        progress = 0.1f;
        initComplete = false;
        initError = null;

        animTimer = new Timer(25, e -> {
            if (!initComplete && progress < 0.85f) {
                progress += 0.022f;
            } else if (initComplete && progress < 1.0f) {
                progress = Math.min(1.0f, progress + 0.06f);
            }
            repaint();
            if (initComplete && progress >= 1.0f) {
                animTimer.stop();
                if (initError != null) {
                    com.womensafety.ui.dialogs.BaseModalDialog.showMessage(
                            null, "Database Error", initError, true);
                }
                if (onNavigateNext != null) {
                    onNavigateNext.run();
                }
            }
        });
        animTimer.start();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DatabaseInitializer.initialize();
                if (!DatabaseManager.testConnection()) {
                    throw new IllegalStateException("Could not open SQLite database connection.");
                }
                Thread.sleep(450);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    initError = "Failed to initialize SQLite database: " + ex.getMessage();
                } finally {
                    initComplete = true;
                }
            }
        }.execute();
    }

    public boolean isAuthenticated() {
        return SessionManager.isAuthenticated();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Subtle dark ambient arcs matching Screen 01
        int w = getWidth();
        int h = getHeight();

        RadialGradientPaint topGlow = new RadialGradientPaint(
                new Point2D.Double(0, h * 0.06),
                210f,
                new float[]{0f, 1f},
                new Color[]{new Color(0xE8, 0xA9, 0x9D, 28), new Color(0x08, 0x08, 0x08, 0)}
        );
        g2.setPaint(topGlow);
        g2.fillRect(0, 0, w, h);

        RadialGradientPaint bottomGlow = new RadialGradientPaint(
                new Point2D.Double(w, h * 0.72),
                240f,
                new float[]{0f, 1f},
                new Color[]{new Color(0xE8, 0xA9, 0x9D, 26), new Color(0x08, 0x08, 0x08, 0)}
        );
        g2.setPaint(bottomGlow);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(0xEC, 0xA9, 0x9C, 55));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new Arc2D.Double(-90, -40, 220, 220, 240, 140, Arc2D.OPEN));
        g2.draw(new Arc2D.Double(w - 170, h * 0.56, 260, 260, 75, 130, Arc2D.OPEN));

        g2.dispose();
    }
}
