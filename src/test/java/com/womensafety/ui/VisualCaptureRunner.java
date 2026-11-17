package com.womensafety.ui;

import com.womensafety.config.DatabaseConfig;
import com.womensafety.database.DatabaseInitializer;
import com.womensafety.models.LocationRecord;
import com.womensafety.models.SOSRecord;
import com.womensafety.models.User;
import com.womensafety.services.AuthService;
import com.womensafety.services.ContactService;
import com.womensafety.services.LocationService;
import com.womensafety.services.SOSService;
import com.womensafety.services.SessionManager;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Automated UI visual verification runner (test scope only).
 * Launches the real AppFrame on Xvfb using a temporary SQLite database,
 * exercises real user journeys, and captures PNGs of all 9 main screens for visual inspection.
 */
public class VisualCaptureRunner {

    public static void main(String[] args) throws Exception {
        Path outDir = Path.of(".visual-captures");
        Files.createDirectories(outDir);

        Path tempDb = Files.createTempFile("wss-visual-", ".db");
        tempDb.toFile().deleteOnExit();
        DatabaseConfig.setCustomJdbcUrl("jdbc:sqlite:" + tempDb.toAbsolutePath());
        DatabaseInitializer.initialize();

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        final AppFrame[] holder = new AppFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            Theme.setupLookAndFeel();
            AppFrame frame = new AppFrame();
            holder[0] = frame;
            frame.launch();
        });

        AppFrame frame = holder[0];
        Thread.sleep(350);
        captureFrame(frame, outDir.resolve("01-splash.png"));

        // Wait for splash initialization to transition to Login
        Thread.sleep(850);
        captureFrame(frame, outDir.resolve("02-login.png"));

        // Register a real test user and add real contacts/SOS via services (verifying real DB flow)
        AuthService authService = new AuthService();
        ContactService contactService = new ContactService();
        SOSService sosService = new SOSService();
        LocationService locationService = new LocationService();

        User user = authService.register("Abhishek Gupta", "abhishek@gmail.com", "Safety@2026", "Safety@2026").data();
        contactService.addContact(user.getId(), "Mom", "+91 98765 43210", "Mother");
        contactService.addContact(user.getId(), "Father", "+91 91234 56780", "Father");
        contactService.addContact(user.getId(), "Brother", "+91 99887 76655", "Brother");

        LocationRecord loc = locationService.getCurrentLocation(user.getId()).data();
        SOSRecord r1 = sosService.saveSOSRecord(user, loc, SOSRecord.STATUS_ACTIVE).data();
        SOSRecord r2 = sosService.saveSOSRecord(user, loc, SOSRecord.STATUS_ACTIVE).data();
        sosService.cancelSOS(r2.getId(), user.getId());

        SwingUtilities.invokeAndWait(frame::showDashboard);
        Thread.sleep(250);
        captureFrame(frame, outDir.resolve("03-dashboard.png"));

        SwingUtilities.invokeAndWait(frame::startSOS);
        Thread.sleep(600);
        captureFrame(frame, outDir.resolve("04-sos-progress.png"));
        Thread.sleep(900);
        captureFrame(frame, outDir.resolve("04b-sos-complete.png"));

        SwingUtilities.invokeAndWait(frame::showLocation);
        Thread.sleep(250);
        captureFrame(frame, outDir.resolve("05-location.png"));

        SwingUtilities.invokeAndWait(frame::showContacts);
        Thread.sleep(250);
        captureFrame(frame, outDir.resolve("06-contacts.png"));

        SwingUtilities.invokeAndWait(frame::showHistory);
        Thread.sleep(250);
        captureFrame(frame, outDir.resolve("07-history.png"));

        SwingUtilities.invokeAndWait(frame::showSupport);
        Thread.sleep(250);
        captureFrame(frame, outDir.resolve("08-support.png"));

        SwingUtilities.invokeAndWait(frame::showProfile);
        Thread.sleep(250);
        captureFrame(frame, outDir.resolve("09-profile.png"));

        SwingUtilities.invokeAndWait(() -> {
            SessionManager.logout();
            frame.dispose();
        });
        System.out.println("Captured all screen renders to " + outDir.toAbsolutePath());
        System.exit(0);
    }

    private static void captureFrame(AppFrame frame, Path filePath) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int w = frame.getContentPane().getWidth();
            int h = frame.getContentPane().getHeight();
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            frame.getContentPane().paintAll(g2);
            g2.dispose();
            try {
                ImageIO.write(img, "png", new File(filePath.toUri()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
