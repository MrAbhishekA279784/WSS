package com.womensafety.repositories;

import com.womensafety.database.DatabaseManager;
import com.womensafety.models.User;
import com.womensafety.models.UserSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, full_name, email, password_hash, created_at FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(long id) throws SQLException {
        String sql = "SELECT id, full_name, email, password_hash, created_at FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public User createUser(String fullName, String email, String passwordHash, String createdAt) throws SQLException {
        String insertUserSql = "INSERT INTO users (full_name, email, password_hash, created_at) VALUES (?, ?, ?, ?)";
        String insertSettingsSql = "INSERT INTO user_settings (user_id, theme, notification_enabled, location_sharing_enabled) VALUES (?, 'DARK', 1, 1)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement ps = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, fullName.trim());
                    ps.setString(2, email.trim().toLowerCase());
                    ps.setString(3, passwordHash);
                    ps.setString(4, createdAt);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to retrieve generated user ID.");
                        }
                        userId = keys.getLong(1);
                    }
                }

                try (PreparedStatement psSettings = conn.prepareStatement(insertSettingsSql)) {
                    psSettings.setLong(1, userId);
                    psSettings.executeUpdate();
                }

                conn.commit();
                return new User(userId, fullName.trim(), email.trim().toLowerCase(), passwordHash, createdAt);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean updateProfile(long userId, String fullName, String email) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePasswordHash(long userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public UserSettings findOrCreateSettings(long userId) throws SQLException {
        String selectSql = "SELECT id, user_id, theme, notification_enabled, location_sharing_enabled FROM user_settings WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserSettings(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("theme"),
                            rs.getInt("notification_enabled") == 1,
                            rs.getInt("location_sharing_enabled") == 1
                    );
                }
            }
        }

        String insertSql = "INSERT INTO user_settings (user_id, theme, notification_enabled, location_sharing_enabled) VALUES (?, 'DARK', 1, 1)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
            long settingsId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    settingsId = keys.getLong(1);
                }
            }
            return new UserSettings(settingsId, userId, "DARK", true, true);
        }
    }

    public boolean updateSettings(UserSettings settings) throws SQLException {
        String sql = "UPDATE user_settings SET theme = ?, notification_enabled = ?, location_sharing_enabled = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, settings.getTheme());
            ps.setInt(2, settings.isNotificationEnabled() ? 1 : 0);
            ps.setInt(3, settings.isLocationSharingEnabled() ? 1 : 0);
            ps.setLong(4, settings.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("created_at")
        );
    }
}
