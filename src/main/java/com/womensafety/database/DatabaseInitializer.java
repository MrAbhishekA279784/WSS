package com.womensafety.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automatically creates required SQLite tables if they do not exist.
 * Never deletes or drops existing user data on startup.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    created_at TEXT NOT NULL
                );
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS emergency_contacts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    contact_name TEXT NOT NULL,
                    phone_number TEXT NOT NULL,
                    relationship TEXT,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sos_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    latitude REAL,
                    longitude REAL,
                    location_type TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    cancelled_at TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS location_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    address TEXT,
                    location_type TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL UNIQUE,
                    theme TEXT NOT NULL DEFAULT 'DARK',
                    notification_enabled INTEGER NOT NULL DEFAULT 1,
                    location_sharing_enabled INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """);
        }
    }
}
