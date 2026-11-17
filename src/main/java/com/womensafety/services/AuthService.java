package com.womensafety.services;

import com.womensafety.models.User;
import com.womensafety.models.UserSettings;
import com.womensafety.repositories.UserRepository;
import com.womensafety.utils.DateTimeUtils;
import com.womensafety.utils.PasswordUtils;
import com.womensafety.utils.ValidationUtils;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService() {
        this(new UserRepository());
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ServiceResult<User> register(String fullName, String email, String password, String confirmPassword) {
        if (!ValidationUtils.isValidName(fullName)) {
            return ServiceResult.fail("Please enter your full name (at least 2 characters).");
        }
        if (!ValidationUtils.isValidEmail(email)) {
            return ServiceResult.fail("Please enter a valid email address.");
        }
        if (!ValidationUtils.isValidPassword(password)) {
            return ServiceResult.fail("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            return ServiceResult.fail("Passwords do not match.");
        }

        try {
            if (userRepository.findByEmail(email).isPresent()) {
                return ServiceResult.fail("An account with this email already exists.");
            }
            String passwordHash = PasswordUtils.hashPassword(password);
            User created = userRepository.createUser(fullName, email, passwordHash, DateTimeUtils.nowIso());
            UserSettings settings = userRepository.findOrCreateSettings(created.getId());
            SessionManager.login(created, settings);
            return ServiceResult.ok(created, "Account created successfully.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error during registration. Please try again.");
        }
    }

    public ServiceResult<User> login(String email, String password) {
        if (!ValidationUtils.isValidEmail(email)) {
            return ServiceResult.fail("Please enter a valid email address.");
        }
        if (password == null || password.isEmpty()) {
            return ServiceResult.fail("Please enter your password.");
        }

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ServiceResult.fail("Invalid email or password.");
            }
            User user = userOpt.get();
            if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                return ServiceResult.fail("Invalid email or password.");
            }
            UserSettings settings = userRepository.findOrCreateSettings(user.getId());
            SessionManager.login(user, settings);
            return ServiceResult.ok(user, "Login successful.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error while signing in.");
        }
    }

    public ServiceResult<Void> resetPasswordByEmail(String email, String newPassword, String confirmPassword) {
        if (!ValidationUtils.isValidEmail(email)) {
            return ServiceResult.fail("Please enter a valid email address.");
        }
        if (!ValidationUtils.isValidPassword(newPassword)) {
            return ServiceResult.fail("New password must be at least 6 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ServiceResult.fail("Passwords do not match.");
        }

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ServiceResult.fail("No registered account found for this email.");
            }
            String newHash = PasswordUtils.hashPassword(newPassword);
            userRepository.updatePasswordHash(userOpt.get().getId(), newHash);
            return ServiceResult.ok(null, "Password updated. You can now log in.");
        } catch (SQLException e) {
            return ServiceResult.fail("Unable to update password in database.");
        }
    }

    public void logout() {
        SessionManager.logout();
    }
}
