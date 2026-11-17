package com.womensafety.services;

import com.womensafety.models.User;
import com.womensafety.models.UserSettings;
import com.womensafety.repositories.UserRepository;
import com.womensafety.utils.PasswordUtils;
import com.womensafety.utils.ValidationUtils;

import java.sql.SQLException;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this(new UserRepository());
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ServiceResult<User> updateProfile(long userId, String fullName, String email) {
        if (!ValidationUtils.isValidName(fullName)) {
            return ServiceResult.fail("Full name must be at least 2 characters.");
        }
        if (!ValidationUtils.isValidEmail(email)) {
            return ServiceResult.fail("Please enter a valid email address.");
        }

        try {
            Optional<User> existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail.isPresent() && existingByEmail.get().getId() != userId) {
                return ServiceResult.fail("Another account already uses that email.");
            }

            boolean updated = userRepository.updateProfile(userId, fullName, email);
            if (!updated) {
                return ServiceResult.fail("User record not found.");
            }

            Optional<User> reloaded = userRepository.findById(userId);
            if (reloaded.isPresent()) {
                SessionManager.updateUser(reloaded.get());
                return ServiceResult.ok(reloaded.get(), "Profile updated successfully.");
            }
            return ServiceResult.fail("Profile updated, but reload failed.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error while updating profile.");
        }
    }

    public ServiceResult<Void> changePassword(long userId, String currentPassword,
                                              String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isEmpty()) {
            return ServiceResult.fail("Please enter your current password.");
        }
        if (!ValidationUtils.isValidPassword(newPassword)) {
            return ServiceResult.fail("New password must be at least 6 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ServiceResult.fail("New passwords do not match.");
        }
        if (currentPassword.equals(newPassword)) {
            return ServiceResult.fail("New password must differ from current password.");
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.fail("User account not found.");
            }
            User user = userOpt.get();
            if (!PasswordUtils.verifyPassword(currentPassword, user.getPasswordHash())) {
                return ServiceResult.fail("Current password is incorrect.");
            }
            String newHash = PasswordUtils.hashPassword(newPassword);
            userRepository.updatePasswordHash(userId, newHash);
            user.setPasswordHash(newHash);
            SessionManager.updateUser(user);
            return ServiceResult.ok(null, "Password changed successfully.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error while changing password.");
        }
    }

    public ServiceResult<UserSettings> getSettings(long userId) {
        try {
            UserSettings settings = userRepository.findOrCreateSettings(userId);
            SessionManager.updateSettings(settings);
            return ServiceResult.ok(settings);
        } catch (SQLException e) {
            return ServiceResult.fail("Failed to load user settings.");
        }
    }

    public ServiceResult<UserSettings> saveSettings(UserSettings settings) {
        try {
            userRepository.updateSettings(settings);
            SessionManager.updateSettings(settings);
            return ServiceResult.ok(settings, "Settings saved.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error while saving settings.");
        }
    }
}
