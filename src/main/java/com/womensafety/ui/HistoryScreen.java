package com.womensafety.ui;

import com.womensafety.models.SOSRecord;
import com.womensafety.models.User;
import com.womensafety.services.HistoryService;
import com.womensafety.services.ServiceResult;
import com.womensafety.services.SessionManager;
import com.womensafety.ui.components.NavigationBar;
import com.womensafety.ui.components.RoundedPanel;
import com.womensafety.ui.components.ScreenHeader;
import com.womensafety.ui.components.VectorIcons;
import com.womensafety.ui.dialogs.SOSDetailsDialog;
import com.womensafety.utils.DateTimeUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * Screen 07 — SOS History Screen matching reference layout:
 * SQLite-backed SOS alert and cancelled records with emergency red siren or green check badges.
 */
public class HistoryScreen extends JPanel {

    private final HistoryService historyService;
    private final JPanel listContainer;

    public HistoryScreen(HistoryService historyService,
                         Runnable onBack,
                         Consumer<NavigationBar.Tab> onNavigateTab) {
        this.historyService = historyService;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(new ScreenHeader("SOS History", onBack), BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setOpaque(false);
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBorder(BorderFactory.createEmptyBorder(8, 22, 18, 22));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
        add(new NavigationBar(NavigationBar.Tab.HISTORY, onNavigateTab), BorderLayout.SOUTH);
    }

    public void refreshData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        long userId = user.getId();
        new SwingWorker<ServiceResult<List<SOSRecord>>, Void>() {
            @Override
            protected ServiceResult<List<SOSRecord>> doInBackground() {
                return historyService.getSOSHistory(userId);
            }

            @Override
            protected void done() {
                try {
                    ServiceResult<List<SOSRecord>> res = get();
                    if (res.success() && res.data() != null) {
                        renderRecords(res.data());
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void renderRecords(List<SOSRecord> records) {
        listContainer.removeAll();

        if (records.isEmpty()) {
            JPanel emptyBox = new JPanel();
            emptyBox.setOpaque(false);
            emptyBox.setLayout(new BoxLayout(emptyBox, BoxLayout.Y_AXIS));
            emptyBox.setBorder(BorderFactory.createEmptyBorder(80, 12, 20, 12));

            JLabel icon = new JLabel(VectorIcons.get(VectorIcons.Type.HISTORY_CLOCK, 42, Theme.TEXT_MUTED));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel title = new JLabel("No SOS History", SwingConstants.CENTER);
            title.setFont(Theme.bold(15f));
            title.setForeground(Theme.TEXT_PRIMARY);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel desc = new JLabel(
                    "<html><center>Triggered or cancelled SOS alerts will appear here with database timestamps.</center></html>",
                    SwingConstants.CENTER
            );
            desc.setFont(Theme.regular(12.5f));
            desc.setForeground(Theme.TEXT_SECONDARY);
            desc.setAlignmentX(Component.CENTER_ALIGNMENT);
            desc.setMaximumSize(new Dimension(280, 48));

            emptyBox.add(icon);
            emptyBox.add(Box.createVerticalStrut(12));
            emptyBox.add(title);
            emptyBox.add(Box.createVerticalStrut(6));
            emptyBox.add(desc);
            listContainer.add(emptyBox);
        } else {
            for (SOSRecord record : records) {
                listContainer.add(createHistoryCard(record));
                listContainer.add(Box.createVerticalStrut(12));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private RoundedPanel createHistoryCard(SOSRecord record) {
        boolean cancelled = record.isCancelled();
        RoundedPanel card = new RoundedPanel(new BorderLayout(14, 0), Theme.CARD_RADIUS, Theme.BG_CARD_SUBTLE, Theme.BORDER_SUBTLE);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setPreferredSize(new Dimension(340, 70));
        card.setMinimumSize(new Dimension(240, 70));

        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cancelled ? Theme.SUCCESS_BG : Theme.EMERGENCY_BG);
                g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(38, 38));
        badge.add(new JLabel(VectorIcons.get(
                cancelled ? VectorIcons.Type.CHECK_CIRCLE : VectorIcons.Type.SIREN,
                19,
                cancelled ? Theme.SUCCESS : Theme.EMERGENCY
        ), SwingConstants.CENTER), BorderLayout.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(record.getDisplayTitle());
        title.setFont(Theme.bold(14f));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel timeLabel = new JLabel(DateTimeUtils.formatForDisplay(record.getCreatedAt()));
        timeLabel.setFont(Theme.regular(12f));
        timeLabel.setForeground(Theme.TEXT_SECONDARY);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(timeLabel);

        JLabel chevron = new JLabel(VectorIcons.get(VectorIcons.Type.CHEVRON_RIGHT, 16, Theme.TEXT_SECONDARY));

        card.add(badge, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(chevron, BorderLayout.EAST);

        card.enableHover(() -> {
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(HistoryScreen.this);
            new SOSDetailsDialog(owner, record).setVisible(true);
        });
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }
}
