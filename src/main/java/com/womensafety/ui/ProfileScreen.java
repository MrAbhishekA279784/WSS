package com.womensafety.ui;

import com.womensafety.config.AppConfig;
import com.womensafety.models.User;
import com.womensafety.models.UserSettings;
import com.womensafety.services.AuthService;
import com.womensafety.services.ServiceResult;
import com.womensafety.services.SessionManager;
import com.womensafety.services.UserService;
import com.womensafety.ui.components.NavigationBar;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.ui.components.RoundedPanel;
import com.womensafety.ui.components.ScreenHeader;
import com.womensafety.ui.components.VectorIcons;
import com.womensafety.ui.dialogs.AppSettingsDialog;
import com.womensafety.ui.dialogs.BaseModalDialog;
import com.womensafety.ui.dialogs.ChangePasswordDialog;
import com.womensafety.ui.dialogs.EditProfileDialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.function.Consumer;

/**
 * Screen 09 — Profile Screen matching reference layout:
 * Avatar, user name/email, My Profile, App Settings, Change Password, About, Outlined Logout, NavigationBar.
 */
public class ProfileScreen extends JPanel {

    private final UserService userService;
    private final AuthService authService;
    private final Runnable onLogout;

    private final JLabel nameLabel;
    private final JLabel emailLabel;

    public ProfileScreen(UserService userService,
                         AuthService authService,
                         Runnable onBack,
                         Runnable onLogout,
                         Consumer<NavigationBar.Tab> onNavigateTab) {
        this.userService = userService;
        this.authService = authService;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(new ScreenHeader("Profile", onBack), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(4, 22, 20, 22));

        // Centered Circular Avatar
        JPanel avatar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x3B, 0x31, 0x2F));
                g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(72, 72));
        avatar.setMaximumSize(new Dimension(72, 72));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel avatarIcon = new JLabel(VectorIcons.get(VectorIcons.Type.PROFILE_USER, 38, Theme.TEXT_PRIMARY), SwingConstants.CENTER);
        avatar.add(avatarIcon, BorderLayout.CENTER);

        nameLabel = new JLabel("User Name", SwingConstants.CENTER);
        nameLabel.setFont(Theme.bold(17f));
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailLabel = new JLabel("user@example.com", SwingConstants.CENTER);
        emailLabel.setFont(Theme.regular(12.5f));
        emailLabel.setForeground(Theme.TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(avatar);
        content.add(Box.createVerticalStrut(12));
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(emailLabel);
        content.add(Box.createVerticalStrut(24));

        content.add(createMenuCard(VectorIcons.Type.PROFILE_USER, "My Profile", this::openEditProfile));
        content.add(Box.createVerticalStrut(10));
        content.add(createMenuCard(VectorIcons.Type.SETTINGS_GEAR, "App Settings", this::openAppSettings));
        content.add(Box.createVerticalStrut(10));
        content.add(createMenuCard(VectorIcons.Type.LOCK, "Change Password", this::openChangePassword));
        content.add(Box.createVerticalStrut(10));
        content.add(createMenuCard(VectorIcons.Type.INFO, "About", this::openAbout));
        content.add(Box.createVerticalStrut(22));

        PrimaryButton logoutBtn = new PrimaryButton(
                "Logout",
                PrimaryButton.Style.OUTLINED_PEACH,
                VectorIcons.get(VectorIcons.Type.LOGOUT, 17, Theme.PEACH_PRIMARY)
        );
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> handleLogout());
        content.add(logoutBtn);
        content.add(Box.createVerticalGlue());

        add(content, BorderLayout.CENTER);
        add(new NavigationBar(NavigationBar.Tab.PROFILE, onNavigateTab), BorderLayout.SOUTH);
    }

    private RoundedPanel createMenuCard(VectorIcons.Type iconType, String title, Runnable onClick) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(14, 0), Theme.INPUT_RADIUS, Theme.BG_CARD_SUBTLE, Theme.BORDER_SUBTLE);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        card.setPreferredSize(new Dimension(340, 52));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLbl = new JLabel(VectorIcons.get(iconType, 18, Theme.TEXT_SECONDARY));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.regular(14f));
        titleLbl.setForeground(Theme.TEXT_PRIMARY);
        JLabel chevron = new JLabel(VectorIcons.get(VectorIcons.Type.CHEVRON_RIGHT, 15, Theme.TEXT_SECONDARY));

        card.add(iconLbl, BorderLayout.WEST);
        card.add(titleLbl, BorderLayout.CENTER);
        card.add(chevron, BorderLayout.EAST);
        card.enableHover(onClick);
        return card;
    }

    public void refreshData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        nameLabel.setText(user.getFullName());
        emailLabel.setText(user.getEmail());
    }

    private void openEditProfile() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        EditProfileDialog dialog = new EditProfileDialog(owner, user, userService);
        dialog.setVisible(true);
        if (dialog.isUpdated()) {
            refreshData();
        }
    }

    private void openAppSettings() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        ServiceResult<UserSettings> res = userService.getSettings(user.getId());
        UserSettings settings = res.success() && res.data() != null ? res.data() : new UserSettings();
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        new AppSettingsDialog(owner, settings, userService).setVisible(true);
    }

    private void openChangePassword() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        ChangePasswordDialog dialog = new ChangePasswordDialog(owner, user.getId(), userService);
        dialog.setVisible(true);
        if (dialog.isUpdated()) {
            BaseModalDialog.showMessage(owner, "Password Updated", "Your password has been changed securely.", false);
        }
    }

    private void openAbout() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        BaseModalDialog.showMessage(
                owner,
                "About " + AppConfig.APP_NAME,
                AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION + "\n"
                        + AppConfig.APP_SUBTITLE + "\n\n"
                        + "Built with Java 21, Java Swing, FlatLaf, and local SQLite persistence.\n"
                        + "Location uses simulated coordinates when no GPS receiver is attached.",
                false
        );
    }

    private void handleLogout() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        boolean confirmed = BaseModalDialog.showConfirm(
                owner,
                "Logout",
                "Are you sure you want to log out of your account?",
                "Logout",
                false
        );
        if (confirmed) {
            authService.logout();
            if (onLogout != null) {
                onLogout.run();
            }
        }
    }
}
