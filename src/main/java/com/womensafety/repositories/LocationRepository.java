package com.womensafety.repositories;

import com.womensafety.database.DatabaseManager;
import com.womensafety.models.LocationRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationRepository {

    public LocationRecord save(long userId, double latitude, double longitude,
                               String address, String locationType, String createdAt) throws SQLException {
        String sql = """
                INSERT INTO location_history (user_id, latitude, longitude, address, location_type, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setDouble(2, latitude);
            ps.setDouble(3, longitude);
            ps.setString(4, address);
            ps.setString(5, locationType);
            ps.setString(6, createdAt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                long id = keys.next() ? keys.getLong(1) : 0;
                return new LocationRecord(id, userId, latitude, longitude, address, locationType, createdAt);
            }
        }
    }

    public Optional<LocationRecord> findLatestByUserId(long userId) throws SQLException {
        String sql = """
                SELECT id, user_id, latitude, longitude, address, location_type, created_at
                FROM location_history
                WHERE user_id = ?
                ORDER BY id DESC
                LIMIT 1
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRecord(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<LocationRecord> findByUserId(long userId) throws SQLException {
        String sql = """
                SELECT id, user_id, latitude, longitude, address, location_type, created_at
                FROM location_history
                WHERE user_id = ?
                ORDER BY id DESC
                """;
        List<LocationRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRecord(rs));
                }
            }
        }
        return list;
    }

    private LocationRecord mapRecord(ResultSet rs) throws SQLException {
        return new LocationRecord(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("address"),
                rs.getString("location_type"),
                rs.getString("created_at")
        );
    }
}
