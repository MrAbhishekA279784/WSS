package com.womensafety.ui;

import com.womensafety.models.EmergencyContact;
import com.womensafety.models.SOSRecord;
import com.womensafety.models.User;
import com.womensafety.services.ContactService;
import com.womensafety.services.HistoryService;
import com.womensafety.services.SessionManager;
import com.womensafety.ui.components.NavigationBar;
import com.womensafety.ui.components.RoundedPanel;
import com.womensafety.ui.components.SOSButton;
import com.womensafety.ui.components.VectorIcons;
import com.womensafety.ui.dialogs.NotificationsDialog;
import com.womensafety.ui.dialogs.SOSConfirmationDialog;

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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Screen 03 — Home Dashboard matching reference layout:
 * Greeting, Bell notification button, central glowing SOS button, 2x2 action cards, bottom navigation.
 */
public class DashboardScreen extends JPanel {

    private final JLabel greetingLabel;
    private final BellButton bellButton;
    private final ContactService contactService;
    private final HistoryService historyService;
    private List<SOSRecord> cachedSosHistory = Collections.emptyList();
    private List<EmergencyContact> cachedContacts = Collections.emptyList();

    public DashboardScreen(ContactService contactService,
                           HistoryService historyService,
                           Runnable onTriggerSOS,
                           Runnable onOpenEmergencySupport,
                           Consumer<NavigationBar.Tab> onNavigateTab) {
        this.contactService = contactService;
        this.historyService = historyService;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        // Top Greeting + Notification Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(22, 24, 10, 20));

        JPanel greetingBlock = new JPanel();
        greetingBlock.setOpaque(false);
        greetingBlock.setLayout(new BoxLayout(greetingBlock, BoxLayout.Y_AXIS));

        greetingLabel = new JLabel("Hello, User");
        greetingLabel.setFont(Theme.bold(20f));
        greetingLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Stay Safe, Stay Connected");
        subtitleLabel.setFont(Theme.regular(12f));
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        greetingBlock.add(greetingLabel);
        greetingBlock.add(Box.createVerticalStrut(3));
        greetingBlock.add(subtitleLabel);

        bellButton = new BellButton();
        bellButton.addActionListener(e -> openNotifications());

        topBar.add(greetingBlock, BorderLayout.WEST);
        topBar.add(bellButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center SOS Button + 2x2 Quick Action Grid
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 24, 18, 24));

        SOSButton sosButton = new SOSButton();
        sosButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sosButton.addActionListener(e -> {
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(DashboardScreen.this);
            SOSConfirmationDialog confirm = new SOSConfirmationDialog(owner);
            confirm.setVisible(true);
            if (confirm.isConfirmed() && onTriggerSOS != null) {
                onTriggerSOS.run();
            }
        });

        JLabel tapPrompt = new JLabel("Tap to Send SOS", SwingConstants.CENTER);
        tapPrompt.setFont(Theme.regular(13.5f));
        tapPrompt.setForeground(Theme.TEXT_PRIMARY);
        tapPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 224));
        grid.setPreferredSize(new Dimension(360, 220));

        grid.add(createActionCard(
                VectorIcons.Type.LOCATION_PIN,
                "Live Location",
                () -> onNavigateTab.accept(NavigationBar.Tab.LOCATION)
        ));
        grid.add(createActionCard(
                VectorIcons.Type.CONTACTS,
                "Contacts",
                () -> onNavigateTab.accept(NavigationBar.Tab.CONTACTS)
        ));
        grid.add(createActionCard(
                VectorIcons.Type.HISTORY_CLOCK,
                "SOS History",
                () -> onNavigateTab.accept(NavigationBar.Tab.HISTORY)
        ));
        grid.add(createActionCard(
                VectorIcons.Type.PHONE,
                "<html><center>Emergency<br/>Support</center></html>",
                onOpenEmergencySupport
        ));

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(sosButton);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(tapPrompt);
        centerPanel.add(Box.createVerticalStrut(24));
        centerPanel.add(grid);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
        add(new NavigationBar(NavigationBar.Tab.HOME, onNavigateTab), BorderLayout.SOUTH);
    }

    private RoundedPanel createActionCard(VectorIcons.Type iconType, String titleHtmlOrText, Runnable onClick) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(), Theme.CARD_RADIUS, Theme.BG_CARD_SUBTLE, Theme.BORDER_SUBTLE);
        card.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel iconLbl = new JLabel(VectorIcons.get(iconType, 24, Theme.PEACH_PRIMARY));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLbl = new JLabel(titleHtmlOrText, SwingConstants.CENTER);
        textLbl.setFont(Theme.regular(13f));
        textLbl.setForeground(Theme.TEXT_PRIMARY);
        textLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(Box.createVerticalGlue());
        inner.add(iconLbl);
        inner.add(Box.createVerticalStrut(10));
        inner.add(textLbl);
        inner.add(Box.createVerticalGlue());

        card.add(inner, BorderLayout.CENTER);
        card.enableHover(onClick);
        return card;
    }

    public void refreshData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        greetingLabel.setText("Hello, " + user.getFirstName());

        long userId = user.getId();
        new SwingWorker<Void, Void>() {
            private List<SOSRecord> sosList;
            private List<EmergencyContact> contactList;

            @Override
            protected Void doInBackground() {
                sosList = historyService.getSOSHistoryOrEmpty(userId);
                contactList = contactService.getContactsOrEmpty(userId);
                return null;
            }

            @Override
            protected void done() {
                cachedSosHistory = sosList != null ? sosList : Collections.emptyList();
                cachedContacts = contactList != null ? contactList : Collections.emptyList();
                boolean hasRealNotice = cachedContacts.isEmpty() || !cachedSosHistory.isEmpty();
                bellButton.setHasBadge(hasRealNotice);
            }
        }.execute();
    }

    private void openNotifications() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        new NotificationsDialog(owner, cachedSosHistory, cachedContacts).setVisible(true);
    }

    private static class BellButton extends JButton {
        private boolean hasBadge = false;

        BellButton() {
            super(VectorIcons.get(VectorIcons.Type.BELL, 21, Theme.TEXT_PRIMARY));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(36, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Notifications");
        }

        void setHasBadge(boolean hasBadge) {
            this.hasBadge = hasBadge;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (hasBadge) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PEACH_PRIMARY);
                g2.fill(new Ellipse2D.Double(getWidth() - 12, 6, 7, 7));
                g2.dispose();
            }
        }
    }
}
