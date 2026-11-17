package com.womensafety.ui;

import com.womensafety.models.User;
import com.womensafety.services.AuthService;
import com.womensafety.services.ServiceResult;
import com.womensafety.ui.components.IconInputField;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.ui.components.ScreenHeader;
import com.womensafety.ui.components.VectorIcons;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Supporting Registration Screen matching the exact Login Screen design system.
 */
public class RegisterScreen extends JPanel {

    private final IconInputField nameField;
    private final IconInputField emailField;
    private final IconInputField passwordField;
    private final IconInputField confirmField;
    private final JLabel errorLabel;
    private final PrimaryButton registerButton;
    private final AuthService authService;
    private final Runnable onRegisterSuccess;
    private final Runnable onBackToLogin;

    public RegisterScreen(AuthService authService, Runnable onRegisterSuccess, Runnable onBackToLogin) {
        this.authService = authService;
        this.onRegisterSuccess = onRegisterSuccess;
        this.onBackToLogin = onBackToLogin;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(new ScreenHeader("", onBackToLogin), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 28, 28, 28));

        JLabel heading = new JLabel("Create Account");
        heading.setFont(Theme.bold(24f));
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Register to stay safe and connected");
        subtitle.setFont(Theme.regular(13.5f));
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameField = new IconInputField(VectorIcons.Type.PROFILE_USER, "Full Name", false);
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = new IconInputField(VectorIcons.Type.EMAIL, "Email", false);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new IconInputField(VectorIcons.Type.LOCK, "Password (min 6 characters)", true);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        confirmField = new IconInputField(VectorIcons.Type.LOCK, "Confirm Password", true);
        confirmField.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmField.addActionListener(e -> performRegister());

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.regular(12f));
        errorLabel.setForeground(Theme.EMERGENCY);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        registerButton = new PrimaryButton("Create Account", PrimaryButton.Style.PRIMARY_PEACH);
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.addActionListener(e -> performRegister());

        JLabel loginLink = new JLabel("Already have an account? Login");
        loginLink.setFont(Theme.regular(12.5f));
        loginLink.setForeground(Theme.PEACH_PRIMARY);
        loginLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onBackToLogin != null) {
                    onBackToLogin.run();
                }
            }
        });

        content.add(heading);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(24));
        content.add(nameField);
        content.add(Box.createVerticalStrut(12));
        content.add(emailField);
        content.add(Box.createVerticalStrut(12));
        content.add(passwordField);
        content.add(Box.createVerticalStrut(12));
        content.add(confirmField);
        content.add(Box.createVerticalStrut(8));
        content.add(errorLabel);
        content.add(Box.createVerticalStrut(14));
        content.add(registerButton);
        content.add(Box.createVerticalStrut(18));
        content.add(loginLink);
        content.add(Box.createVerticalGlue());

        add(content, BorderLayout.CENTER);
    }

    public void resetForm() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmField.clear();
        errorLabel.setText(" ");
    }

    private void performRegister() {
        errorLabel.setText(" ");
        registerButton.setEnabled(false);

        String name = nameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();
        String confirm = confirmField.getText();

        new SwingWorker<ServiceResult<User>, Void>() {
            @Override
            protected ServiceResult<User> doInBackground() {
                return authService.register(name, email, pass, confirm);
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                try {
                    ServiceResult<User> res = get();
                    if (res.success()) {
                        resetForm();
                        if (onRegisterSuccess != null) {
                            onRegisterSuccess.run();
                        }
                    } else {
                        errorLabel.setText(res.message());
                    }
                } catch (Exception ex) {
                    errorLabel.setText("Registration failed. Please try again.");
                }
            }
        }.execute();
    }
}
