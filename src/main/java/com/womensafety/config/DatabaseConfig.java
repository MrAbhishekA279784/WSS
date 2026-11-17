package com.womensafety.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SQLite database path and JDBC URL configuration.
 */
public final class DatabaseConfig {

    private static final String DEFAULT_DB_FILE = "women_safety.db";
    private static volatile String customJdbcUrl = null;

    private DatabaseConfig() {
    }

    public static String getDatabasePath() {
        String override = System.getProperty("wss.db.path");
        if (override != null && !override.isBlank()) {
            return override.trim();
        }
        Path path = Paths.get(System.getProperty("user.dir"), DEFAULT_DB_FILE);
        return path.toAbsolutePath().toString();
    }

    public static String getJdbcUrl() {
        if (customJdbcUrl != null) {
            return customJdbcUrl;
        }
        return "jdbc:sqlite:" + getDatabasePath();
    }

    /**
     * Allows unit/integration tests to isolate into a temporary database file.
     */
    public static void setCustomJdbcUrl(String jdbcUrl) {
        customJdbcUrl = jdbcUrl;
    }

    public static void resetToDefault() {
        customJdbcUrl = null;
    }
}
