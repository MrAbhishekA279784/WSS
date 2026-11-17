package com.womensafety.services;

import com.womensafety.models.User;
import com.womensafety.models.UserSettings;

/**
 * Holds the active authenticated user session in memory.
 */
public final class SessionManager {

    private static volatile User currentUser;
    private static volatile UserSettings currentSettings;

    private SessionManager() {
    }

    public static synchronized void login(User user, UserSettings settings) {
        currentUser = user;
        currentSettings = settings;
    }

    public static synchronized void updateUser(User user) {
        currentUser = user;
    }

    public static synchronized void updateSettings(UserSettings settings) {
        currentSettings = settings;
    }

    public static synchronized void logout() {
        currentUser = null;
        currentSettings = null;
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static UserSettings getCurrentSettings() {
        return currentSettings;
    }
}
