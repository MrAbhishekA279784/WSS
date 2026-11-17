package com.womensafety.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Application-wide configuration, palette constants, and configurable emergency numbers.
 */
public final class AppConfig {

    public static final String APP_NAME = "Women Safety";
    public static final String APP_SUBTITLE = "Your Personal Safety Companion";
    public static final String APP_VERSION = "1.0.0";

    // Fixed viewport proportions matching reference UI
    public static final int WINDOW_WIDTH = 420;
    public static final int WINDOW_HEIGHT = 840;

    // Configurable Emergency Service Numbers (key = label, value = dial number)
    private static final Map<String, String> EMERGENCY_NUMBERS = new LinkedHashMap<>();

    static {
        EMERGENCY_NUMBERS.put("Police", System.getProperty("wss.emergency.police", "100"));
        EMERGENCY_NUMBERS.put("Women Helpline", System.getProperty("wss.emergency.women", "181"));
        EMERGENCY_NUMBERS.put("Ambulance", System.getProperty("wss.emergency.ambulance", "108"));
        EMERGENCY_NUMBERS.put("Emergency (All Services)", System.getProperty("wss.emergency.all", "112"));
    }

    // Default simulated location preset (clearly identified as simulated when hardware GPS is absent)
    public static final double DEFAULT_SIMULATED_LAT = 19.2638;
    public static final double DEFAULT_SIMULATED_LNG = 72.9629;
    public static final String DEFAULT_SIMULATED_ADDRESS = "Simulated Preset: Kandivali (E), Mumbai";

    // Optional external notification gateway configuration via environment variables
    // Never hardcode credentials; if absent, NotificationService reports "Not configured".
    public static boolean isSmsGatewayConfigured() {
        String endpoint = System.getenv("WSS_SMS_WEBHOOK_URL");
        return endpoint != null && !endpoint.isBlank();
    }

    public static boolean isEmailGatewayConfigured() {
        String host = System.getenv("WSS_SMTP_HOST");
        return host != null && !host.isBlank();
    }

    public static Map<String, String> getEmergencyNumbers() {
        return Collections.unmodifiableMap(EMERGENCY_NUMBERS);
    }

    private AppConfig() {
    }
}
