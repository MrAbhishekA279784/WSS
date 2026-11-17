package com.womensafety.repositories;

import com.womensafety.database.DatabaseManager;
import com.womensafety.models.SOSRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SOSRepository {

    public SOSRecord create(long userId, String status, Double latitude, Double longitude,
                            String locationType, String createdAt) throws SQLException {
        String sql = """
                INSERT INTO sos_history (user_id, status, latitude, longitude, location_type, created_at, cancelled_at)
                VALUES (?, ?, ?, ?, ?, ?, NULL)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setString(2, status);
            if (latitude != null) {
                ps.setDouble(3, latitude);
            } else {
                ps.setNull(3, Types.REAL);
            }
            if (longitude != null) {
                ps.setDouble(4, longitude);
            } else {
                ps.setNull(4, Types.REAL);
            }
            ps.setString(5, locationType);
            ps.setString(6, createdAt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                long id = keys.next() ? keys.getLong(1) : 0;
                return new SOSRecord(id, userId, status, latitude, longitude, locationType, createdAt, null);
            }
        }
    }

    public boolean updateStatus(long sosId, long userId, String status, String cancelledAt) throws SQLException {
        String sql = "UPDATE sos_history SET status = ?, cancelled_at = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (cancelledAt != null) {
                ps.setString(2, cancelledAt);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setLong(3, sosId);
            ps.setLong(4, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<SOSRecord> findByUserId(long userId) throws SQLException {
        String sql = """
                SELECT id, user_id, status, latitude, longitude, location_type, created_at, cancelled_at
                FROM sos_history
                WHERE user_id = ?
                ORDER BY id DESC
                """;
        List<SOSRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRecord(rs));
                }
            }
        }
        return records;
    }

    private SOSRecord mapRecord(ResultSet rs) throws SQLException {
        Object latObj = rs.getObject("latitude");
        Object lngObj = rs.getObject("longitude");
        Double lat = latObj != null ? ((Number) latObj).doubleValue() : null;
        Double lng = lngObj != null ? ((Number) lngObj).doubleValue() : null;
        return new SOSRecord(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("status"),
                lat,
                lng,
                rs.getString("location_type"),
                rs.getString("created_at"),
                rs.getString("cancelled_at")
        );
    }
}
