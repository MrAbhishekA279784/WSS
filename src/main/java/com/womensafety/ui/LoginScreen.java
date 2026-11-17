package com.womensafety.ui;

import com.womensafety.models.User;
import com.womensafety.services.AuthService;
import com.womensafety.services.ServiceResult;
import com.womensafety.ui.components.IconInputField;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.ui.components.ScreenHeader;
import com.womensafety.ui.components.VectorIcons;
import com.womensafety.ui.dialogs.ForgotPasswordDialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Screen 02 — Login Screen matching reference layout and typography.
 */
public class LoginScreen extends JPanel {

    private final IconInputField emailField;
    private final IconInputField passwordField;
    private final JLabel errorLabel;
    private final PrimaryButton loginButton;
    private final AuthService authService;
    private final Runnable onLoginSuccess;

    public LoginScreen(AuthService authService,
                       Runnable onLoginSuccess,
                       Runnable onGoToRegister,
                       Runnable onBackToSplash) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        // Top-left back chevron matching Screen 02 of reference
        add(new ScreenHeader("", onBackToSplash), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(18, 28, 32, 28));

        JLabel heading = new JLabel("Welcome Back");
        heading.setFont(Theme.bold(25f));
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Login to continue");
        subtitle.setFont(Theme.regular(13.5f));
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = new IconInputField(VectorIcons.Type.EMAIL, "Email", false);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new IconInputField(VectorIcons.Type.LOCK, "Password", true);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.addActionListener(e -> performLogin());

        // Right-aligned Forgot Password link
        JPanel forgotRow = new JPanel(new BorderLayout());
        forgotRow.setOpaque(false);
        forgotRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        forgotRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel forgotLink = new JLabel("Forgot Password?", SwingConstants.RIGHT);
        forgotLink.setFont(Theme.regular(12.5f));
        forgotLink.setForeground(Theme.PEACH_PRIMARY);
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(LoginScreen.this);
                new ForgotPasswordDialog(owner, authService).setVisible(true);
            }
        });
        forgotRow.add(forgotLink, BorderLayout.EAST);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.regular(12f));
        errorLabel.setForeground(Theme.EMERGENCY);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginButton = new PrimaryButton("Login", PrimaryButton.Style.PRIMARY_PEACH);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        JPanel orDivider = createOrDivider();
        orDivider.setAlignmentX(Component.LEFT_ALIGNMENT);

        PrimaryButton createAccountBtn = new PrimaryButton(
                "Create New Account",
                PrimaryButton.Style.OUTLINED_PEACH,
                VectorIcons.get(VectorIcons.Type.PLUS, 16, Theme.TEXT_PRIMARY)
        );
        createAccountBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        createAccountBtn.addActionListener(e -> {
            clearErrors();
            if (onGoToRegister != null) {
                onGoToRegister.run();
            }
        });

        content.add(heading);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(30));
        content.add(emailField);
        content.add(Box.createVerticalStrut(14));
        content.add(passwordField);
        content.add(Box.createVerticalStrut(10));
        content.add(forgotRow);
        content.add(Box.createVerticalStrut(8));
        content.add(errorLabel);
        content.add(Box.createVerticalStrut(14));
        content.add(loginButton);
        content.add(Box.createVerticalStrut(22));
        content.add(orDivider);
        content.add(Box.createVerticalStrut(22));
        content.add(createAccountBtn);
        content.add(Box.createVerticalGlue());

        add(content, BorderLayout.CENTER);
    }

    private JPanel createOrDivider() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JSeparator left = new JSeparator();
        left.setForeground(Theme.BORDER_MUTED);
        left.setBackground(Theme.BORDER_MUTED);
        panel.add(left, gbc);

        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel orLabel = new JLabel("  or  ");
        orLabel.setFont(Theme.regular(12f));
        orLabel.setForeground(Theme.TEXT_SECONDARY);
        panel.add(orLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JSeparator right = new JSeparator();
        right.setForeground(Theme.BORDER_MUTED);
        right.setBackground(Theme.BORDER_MUTED);
        panel.add(right, gbc);

        return panel;
    }

    public void resetForm() {
        emailField.clear();
        passwordField.clear();
        clearErrors();
    }

    private void clearErrors() {
        errorLabel.setText(" ");
    }

    private void performLogin() {
        clearErrors();
        loginButton.setEnabled(false);
        String email = emailField.getText();
        String password = passwordField.getText();

        new SwingWorker<ServiceResult<User>, Void>() {
            @Override
            protected ServiceResult<User> doInBackground() {
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    ServiceResult<User> res = get();
                    if (res.success()) {
                        passwordField.clear();
                        if (onLoginSuccess != null) {
                            onLoginSuccess.run();
                        }
                    } else {
                        errorLabel.setText(res.message());
                    }
                } catch (Exception ex) {
                    errorLabel.setText("Sign in failed. Please try again.");
                }
            }
        }.execute();
    }
}
