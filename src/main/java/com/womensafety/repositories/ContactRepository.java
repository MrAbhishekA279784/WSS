package com.womensafety.repositories;

import com.womensafety.database.DatabaseManager;
import com.womensafety.models.EmergencyContact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ContactRepository {

    public List<EmergencyContact> findByUserId(long userId) throws SQLException {
        String sql = """
                SELECT id, user_id, contact_name, phone_number, relationship, created_at
                FROM emergency_contacts
                WHERE user_id = ?
                ORDER BY id ASC
                """;
        List<EmergencyContact> contacts = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contacts.add(mapContact(rs));
                }
            }
        }
        return contacts;
    }

    public boolean existsByUserAndPhone(long userId, String normalizedPhone, Long excludeContactId) throws SQLException {
        List<EmergencyContact> existing = findByUserId(userId);
        for (EmergencyContact contact : existing) {
            if (excludeContactId != null && contact.getId() == excludeContactId) {
                continue;
            }
            String existingNormalized = contact.getPhoneNumber().replaceAll("[^0-9]", "");
            if (existingNormalized.equals(normalizedPhone)) {
                return true;
            }
        }
        return false;
    }

    public EmergencyContact create(long userId, String name, String phoneNumber,
                                   String relationship, String createdAt) throws SQLException {
        String sql = """
                INSERT INTO emergency_contacts (user_id, contact_name, phone_number, relationship, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setString(2, name.trim());
            ps.setString(3, phoneNumber.trim());
            ps.setString(4, relationship == null ? "" : relationship.trim());
            ps.setString(5, createdAt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                long id = keys.next() ? keys.getLong(1) : 0;
                return new EmergencyContact(id, userId, name.trim(), phoneNumber.trim(),
                        relationship == null ? "" : relationship.trim(), createdAt);
            }
        }
    }

    public boolean update(long contactId, long userId, String name, String phoneNumber, String relationship) throws SQLException {
        String sql = """
                UPDATE emergency_contacts
                SET contact_name = ?, phone_number = ?, relationship = ?
                WHERE id = ? AND user_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, phoneNumber.trim());
            ps.setString(3, relationship == null ? "" : relationship.trim());
            ps.setLong(4, contactId);
            ps.setLong(5, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long contactId, long userId) throws SQLException {
        String sql = "DELETE FROM emergency_contacts WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contactId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private EmergencyContact mapContact(ResultSet rs) throws SQLException {
        return new EmergencyContact(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("contact_name"),
                rs.getString("phone_number"),
                rs.getString("relationship"),
                rs.getString("created_at")
        );
    }
}
