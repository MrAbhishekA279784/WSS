package com.womensafety.ui.components;

import com.womensafety.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Consistent 5-item bottom navigation bar matching reference UI order:
 * Home, Location, Contacts, History, Profile.
 */
public class NavigationBar extends JPanel {

    public enum Tab {
        HOME("Home", VectorIcons.Type.HOME),
        LOCATION("Location", VectorIcons.Type.LOCATION_PIN),
        CONTACTS("Contacts", VectorIcons.Type.CONTACTS),
        HISTORY("History", VectorIcons.Type.HISTORY_CLOCK),
        PROFILE("Profile", VectorIcons.Type.PROFILE_USER);

        private final String label;
        private final VectorIcons.Type iconType;

        Tab(String label, VectorIcons.Type iconType) {
            this.label = label;
            this.iconType = iconType;
        }
    }

    private Tab activeTab;
    private final Consumer<Tab> onSelect;
    private final Map<Tab, NavItemPanel> itemPanels = new EnumMap<>(Tab.class);

    public NavigationBar(Tab initialTab, Consumer<Tab> onSelect) {
        super(new GridLayout(1, 5, 0, 0));
        this.activeTab = initialTab;
        this.onSelect = onSelect;
        setBackground(new Color(0x0B, 0x0B, 0x0B));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_MUTED),
                BorderFactory.createEmptyBorder(6, 6, 8, 6)
        ));
        setPreferredSize(new Dimension(420, 66));

        for (Tab tab : Tab.values()) {
            NavItemPanel item = new NavItemPanel(tab);
            itemPanels.put(tab, item);
            add(item);
        }
        refreshStyles();
    }

    public void setActiveTab(Tab tab) {
        this.activeTab = tab;
        refreshStyles();
    }

    private void refreshStyles() {
        for (Map.Entry<Tab, NavItemPanel> entry : itemPanels.entrySet()) {
            entry.getValue().setActive(entry.getKey() == activeTab);
        }
    }

    private class NavItemPanel extends JPanel {
        private final Tab tab;
        private boolean active;
        private boolean hovered;
        private final JLabel textLabel;
        private final IconBadge iconBadge;

        NavItemPanel(Tab tab) {
            super(new BorderLayout(0, 2));
            this.tab = tab;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            this.iconBadge = new IconBadge(tab.iconType);
            this.textLabel = new JLabel(tab.label, SwingConstants.CENTER);
            textLabel.setFont(Theme.regular(11f));

            add(iconBadge, BorderLayout.CENTER);
            add(textLabel, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onSelect != null) {
                        onSelect.accept(tab);
                    }
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            iconBadge.setActive(active);
            textLabel.setForeground(active ? Theme.PEACH_PRIMARY : Theme.TEXT_SECONDARY);
            textLabel.setFont(active ? Theme.bold(11f) : Theme.regular(11f));
            repaint();
        }

        private class IconBadge extends JPanel {
            private final VectorIcons.Type iconType;
            private boolean itemActive;

            IconBadge(VectorIcons.Type iconType) {
                this.iconType = iconType;
                setOpaque(false);
                setPreferredSize(new Dimension(34, 30));
            }

            void setActive(boolean active) {
                this.itemActive = active;
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int disc = 28;
                int iconSize = 18;

                if (itemActive) {
                    g2.setColor(Theme.PEACH_PRIMARY);
                    g2.fill(new Ellipse2D.Double(cx - disc / 2.0, cy - disc / 2.0, disc, disc));
                    g2.translate(cx - iconSize / 2.0, cy - iconSize / 2.0);
                    VectorIcons.paint(g2, iconType, iconSize, Theme.TEXT_ON_PEACH);
                } else {
                    if (hovered) {
                        g2.setColor(Theme.BG_CARD);
                        g2.fill(new Ellipse2D.Double(cx - disc / 2.0, cy - disc / 2.0, disc, disc));
                    }
                    g2.translate(cx - iconSize / 2.0, cy - iconSize / 2.0);
                    VectorIcons.paint(g2, iconType, iconSize, hovered ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
                }
                g2.dispose();
            }
        }
    }
}
