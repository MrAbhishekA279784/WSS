package com.womensafety.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void pbkdf2HashingAndVerificationWorkCorrectly() {
        String hash = PasswordUtils.hashPassword("Safety@2026");
        assertNotNull(hash);
        assertNotEquals("Safety@2026", hash);
        assertTrue(hash.contains(":"));

        assertTrue(PasswordUtils.verifyPassword("Safety@2026", hash));
        assertFalse(PasswordUtils.verifyPassword("WrongPassword", hash));
        assertFalse(PasswordUtils.verifyPassword("", hash));
        assertFalse(PasswordUtils.verifyPassword("Safety@2026", "invalid-format"));
    }

    @Test
    void validationUtilsValidatesInputsProperly() {
        assertTrue(ValidationUtils.isValidEmail("ananya.sharma@example.com"));
        assertFalse(ValidationUtils.isValidEmail("not-an-email"));
        assertFalse(ValidationUtils.isValidEmail(""));

        assertTrue(ValidationUtils.isValidPhoneNumber("+91 98765 43210"));
        assertTrue(ValidationUtils.isValidPhoneNumber("1000000"));
        assertFalse(ValidationUtils.isValidPhoneNumber("123"));
        assertFalse(ValidationUtils.isValidPhoneNumber("abc-123-4567"));

        assertTrue(ValidationUtils.isValidPassword("123456"));
        assertFalse(ValidationUtils.isValidPassword("12345"));

        assertTrue(ValidationUtils.isValidCoordinates(19.2638, 72.9629));
        assertFalse(ValidationUtils.isValidCoordinates(95.0, 72.0));
        assertFalse(ValidationUtils.isValidCoordinates(19.0, 190.0));
    }

    @Test
    void mapUtilsGeneratesValidGoogleMapsUrlAndCoordinates() {
        String url = MapUtils.buildGoogleMapsUrl(19.2638, 72.9629);
        assertEquals("https://www.google.com/maps?q=19.263800,72.962900", url);

        String formatted = MapUtils.formatCoordinates(19.2638, 72.9629);
        assertEquals("19.2638° N, 72.9629° E", formatted);

        String southern = MapUtils.formatCoordinates(-33.8688, -151.2093);
        assertEquals("33.8688° S, 151.2093° W", southern);
    }
}
