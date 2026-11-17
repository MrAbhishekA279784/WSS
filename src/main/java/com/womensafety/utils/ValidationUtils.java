package com.womensafety.utils;

import java.util.regex.Pattern;

/**
 * Input validation helpers for authentication, contacts, and coordinates.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_ALLOWED_CHARS =
            Pattern.compile("^[+]?[0-9\\s\\-()]{7,20}$");

    private ValidationUtils() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.trim().length() <= 80;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6 && password.length() <= 128;
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) {
            return false;
        }
        String trimmed = phone.trim();
        if (!PHONE_ALLOWED_CHARS.matcher(trimmed).matches()) {
            return false;
        }
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 7 && digitsOnly.length() <= 15;
    }

    public static String normalizePhoneForDial(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.trim().replaceAll("[\\s\\-()]", "");
    }

    public static boolean isValidCoordinates(double latitude, double longitude) {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude)
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }
}
