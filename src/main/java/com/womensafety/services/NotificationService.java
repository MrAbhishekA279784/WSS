package com.womensafety.services;

import com.womensafety.config.AppConfig;
import com.womensafety.models.EmergencyContact;
import com.womensafety.models.LocationRecord;
import com.womensafety.models.User;
import com.womensafety.utils.MapUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Handles emergency alert delivery strictly and honestly.
 * Never reports successful delivery unless an actual configured gateway responds 2xx.
 */
public class NotificationService {

    public enum DeliveryState {
        DELIVERED,
        NOT_CONFIGURED,
        NO_CONTACTS,
        DISABLED_BY_USER,
        FAILED
    }

    public record NotificationResult(DeliveryState state, String message) {
        public boolean isDelivered() {
            return state == DeliveryState.DELIVERED;
        }
    }

    public NotificationResult sendEmergencyNotifications(User user,
                                                         List<EmergencyContact> contacts,
                                                         LocationRecord location,
                                                         boolean notificationsEnabled) {
        if (!notificationsEnabled) {
            return new NotificationResult(
                    DeliveryState.DISABLED_BY_USER,
                    "Notifications are disabled in App Settings; alert saved locally."
            );
        }

        if (contacts == null || contacts.isEmpty()) {
            return new NotificationResult(
                    DeliveryState.NO_CONTACTS,
                    "No trusted contacts saved. Add contacts to enable alert dispatch."
            );
        }

        if (!AppConfig.isSmsGatewayConfigured()) {
            return new NotificationResult(
                    DeliveryState.NOT_CONFIGURED,
                    "External SMS/Email gateway not configured (WSS_SMS_WEBHOOK_URL unset). SOS saved to database."
            );
        }

        String webhookUrl = System.getenv("WSS_SMS_WEBHOOK_URL").trim();
        String mapsUrl = location != null
                ? MapUtils.buildGoogleMapsUrl(location.getLatitude(), location.getLongitude())
                : "Unavailable";

        String payload = buildJsonPayload(user, contacts, mapsUrl, location);
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new NotificationResult(
                        DeliveryState.DELIVERED,
                        "Emergency alert dispatched to " + contacts.size() + " contact(s)."
                );
            }
            return new NotificationResult(
                    DeliveryState.FAILED,
                    "Gateway returned HTTP " + response.statusCode() + "; SOS saved locally."
            );
        } catch (Exception e) {
            return new NotificationResult(
                    DeliveryState.FAILED,
                    "Notification dispatch failed (" + e.getClass().getSimpleName() + "); SOS saved locally."
            );
        }
    }

    private String buildJsonPayload(User user, List<EmergencyContact> contacts,
                                    String mapsUrl, LocationRecord location) {
        StringBuilder phonesJson = new StringBuilder("[");
        for (int i = 0; i < contacts.size(); i++) {
            if (i > 0) {
                phonesJson.append(",");
            }
            phonesJson.append("\"").append(escapeJson(contacts.get(i).getPhoneNumber())).append("\"");
        }
        phonesJson.append("]");

        String locType = location != null ? location.getLocationType() : "UNAVAILABLE";
        return "{"
                + "\"userName\":\"" + escapeJson(user.getFullName()) + "\","
                + "\"userEmail\":\"" + escapeJson(user.getEmail()) + "\","
                + "\"locationType\":\"" + escapeJson(locType) + "\","
                + "\"mapsUrl\":\"" + escapeJson(mapsUrl) + "\","
                + "\"recipients\":" + phonesJson
                + "}";
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
