package com.womensafety.utils;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/**
 * Google Maps URL generation, coordinate formatting, clipboard copy, and OS URI handlers.
 */
public final class MapUtils {

    public record ActionResult(boolean success, String message) {
    }

    private MapUtils() {
    }

    public static String buildGoogleMapsUrl(double latitude, double longitude) {
        return String.format(Locale.US, "https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
    }

    public static String formatCoordinates(double latitude, double longitude) {
        String latDir = latitude >= 0 ? "N" : "S";
        String lngDir = longitude >= 0 ? "E" : "W";
        return String.format(Locale.US, "%.4f° %s, %.4f° %s",
                Math.abs(latitude), latDir, Math.abs(longitude), lngDir);
    }

    public static ActionResult copyToClipboard(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            return new ActionResult(true, "Copied to clipboard.");
        } catch (Exception e) {
            return new ActionResult(false, "Unable to access system clipboard: " + e.getMessage());
        }
    }

    public static ActionResult openInBrowser(String url) {
        if (!Desktop.isDesktopSupported()) {
            return new ActionResult(false, "Desktop browser integration is not supported on this OS.");
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            return new ActionResult(false, "System browser launch is not supported in this environment.");
        }
        try {
            desktop.browse(URI.create(url));
            return new ActionResult(true, "Opened Google Maps in your default browser.");
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            return new ActionResult(false, "Could not launch browser: " + e.getMessage());
        }
    }

    public static ActionResult initiateSystemCall(String phoneNumber) {
        String normalized = ValidationUtils.normalizePhoneForDial(phoneNumber);
        if (normalized.isEmpty()) {
            return new ActionResult(false, "Invalid phone number.");
        }
        if (!Desktop.isDesktopSupported()) {
            return new ActionResult(false,
                    "System phone handler unavailable. Please dial " + phoneNumber + " manually.");
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            return new ActionResult(false,
                    "OS tel: handler unsupported here. Please dial " + phoneNumber + " manually.");
        }
        try {
            desktop.browse(URI.create("tel:" + normalized));
            return new ActionResult(true,
                    "Handed off tel:" + normalized + " to the operating system dialer.");
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            return new ActionResult(false,
                    "No OS phone application registered for tel: links (" + e.getMessage()
                            + "). Please dial " + phoneNumber + " manually.");
        }
    }
}
