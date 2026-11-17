package com.womensafety.services;

import com.womensafety.config.DatabaseConfig;
import com.womensafety.database.DatabaseInitializer;
import com.womensafety.models.EmergencyContact;
import com.womensafety.models.LocationRecord;
import com.womensafety.models.SOSRecord;
import com.womensafety.models.User;
import com.womensafety.models.UserSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppServicesIntegrationTest {

    @TempDir
    Path tempDir;

    private AuthService authService;
    private UserService userService;
    private ContactService contactService;
    private LocationService locationService;
    private HistoryService historyService;
    private SOSService sosService;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = tempDir.resolve("test_women_safety.db");
        DatabaseConfig.setCustomJdbcUrl("jdbc:sqlite:" + dbFile.toAbsolutePath());
        DatabaseInitializer.initialize();
        SessionManager.logout();

        authService = new AuthService();
        userService = new UserService();
        contactService = new ContactService();
        locationService = new LocationService();
        historyService = new HistoryService();
        sosService = new SOSService();
    }

    @AfterEach
    void tearDown() {
        SessionManager.logout();
        DatabaseConfig.resetToDefault();
    }

    @Test
    void registrationLoginDuplicatePreventionAndPersistenceWork() throws Exception {
        ServiceResult<User> reg = authService.register("Meera Nair", "meera@example.com", "Pass@123", "Pass@123");
        assertTrue(reg.success());
        assertNotNull(reg.data());
        assertTrue(SessionManager.isAuthenticated());

        // Duplicate registration must fail
        ServiceResult<User> dup = authService.register("Meera Duplicate", "MEERA@example.com", "Pass@123", "Pass@123");
        assertFalse(dup.success());

        authService.logout();
        assertFalse(SessionManager.isAuthenticated());

        // Re-running DatabaseInitializer must preserve data
        DatabaseInitializer.initialize();

        // Wrong password must fail
        assertFalse(authService.login("meera@example.com", "WrongPass").success());

        // Correct password succeeds
        ServiceResult<User> login = authService.login("meera@example.com", "Pass@123");
        assertTrue(login.success());
        assertEquals("Meera Nair", login.data().getFullName());
    }

    @Test
    void profileAndPasswordChangeWorkEndToEnd() {
        User user = authService.register("Kavya Patel", "kavya@example.com", "OldPass1", "OldPass1").data();
        assertNotNull(user);

        ServiceResult<User> updated = userService.updateProfile(user.getId(), "Kavya Sharma", "kavya.s@example.com");
        assertTrue(updated.success());
        assertEquals("Kavya Sharma", updated.data().getFullName());

        // Wrong current password rejected
        assertFalse(userService.changePassword(user.getId(), "WrongOld", "NewPass1", "NewPass1").success());

        // Correct current password succeeds
        assertTrue(userService.changePassword(user.getId(), "OldPass1", "NewPass1", "NewPass1").success());

        authService.logout();
        assertFalse(authService.login("kavya.s@example.com", "OldPass1").success());
        assertTrue(authService.login("kavya.s@example.com", "NewPass1").success());
    }

    @Test
    void contactsCrudAndDuplicateValidationWork() {
        User user = authService.register("Riya Sen", "riya@example.com", "Pass@123", "Pass@123").data();
        assertNotNull(user);

        // Starts completely empty (no hardcoded sample contacts)
        assertTrue(contactService.getContacts(user.getId()).data().isEmpty());

        ServiceResult<EmergencyContact> added = contactService.addContact(
                user.getId(), "Mom", "+91 98765 43210", "Mother");
        assertTrue(added.success());

        // Duplicate phone rejected
        ServiceResult<EmergencyContact> dup = contactService.addContact(
                user.getId(), "Mom Backup", "919876543210", "Mother");
        assertFalse(dup.success());

        // Update contact
        assertTrue(contactService.updateContact(
                added.data().getId(), user.getId(), "Mother", "+91 91234 56789", "Parent").success());

        List<EmergencyContact> list = contactService.getContacts(user.getId()).data();
        assertEquals(1, list.size());
        assertEquals("Mother", list.get(0).getContactName());

        // Delete contact
        assertTrue(contactService.deleteContact(added.data().getId(), user.getId()).success());
        assertTrue(contactService.getContacts(user.getId()).data().isEmpty());
    }

    @Test
    void sosWorkflowPersistsRecordCancelsCleanlyAndReportsHonestNotificationStatus() {
        User user = authService.register("Diya Verma", "diya@example.com", "Pass@123", "Pass@123").data();
        assertNotNull(user);
        UserSettings settings = userService.getSettings(user.getId()).data();
        contactService.addContact(user.getId(), "Sister", "+91 99887 76655", "Family");

        ServiceResult<LocationRecord> locRes = sosService.resolveLocationForSOS(user, true);
        assertTrue(locRes.success());
        assertNotNull(locRes.data());
        assertEquals(LocationRecord.TYPE_SIMULATED, locRes.data().getLocationType());

        // Notification must honestly report NOT_CONFIGURED when webhook env var is absent
        NotificationService.NotificationResult notifyRes = sosService.dispatchNotifications(user, settings, locRes.data());
        assertFalse(notifyRes.isDelivered());
        assertEquals(NotificationService.DeliveryState.NOT_CONFIGURED, notifyRes.state());

        // SOS must still be saved to SQLite
        ServiceResult<SOSRecord> saveRes = sosService.saveSOSRecord(user, locRes.data(), SOSRecord.STATUS_ACTIVE);
        assertTrue(saveRes.success());
        assertEquals(SOSRecord.STATUS_ACTIVE, saveRes.data().getStatus());

        // Cancel SOS
        ServiceResult<SOSRecord> cancelRes = sosService.cancelSOS(saveRes.data().getId(), user.getId());
        assertTrue(cancelRes.success());

        List<SOSRecord> history = historyService.getSOSHistory(user.getId()).data();
        assertEquals(1, history.size());
        assertEquals(SOSRecord.STATUS_CANCELLED, history.get(0).getStatus());
        assertNotNull(history.get(0).getCancelledAt());
    }
}
