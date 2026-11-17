package com.womensafety.services;

import com.womensafety.models.EmergencyContact;
import com.womensafety.models.LocationRecord;
import com.womensafety.models.SOSRecord;
import com.womensafety.models.User;
import com.womensafety.models.UserSettings;
import com.womensafety.repositories.SOSRepository;
import com.womensafety.utils.DateTimeUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Coordinates the multi-step SOS workflow:
 * 1) Acquire location (simulated when hardware GPS is unavailable)
 * 2) Persist SOS record in SQLite even if external notification fails
 * 3) Attempt configured contact notification honestly
 * 4) Support SOS cancellation with database timestamp update
 */
public class SOSService {

    private final SOSRepository sosRepository;
    private final LocationService locationService;
    private final ContactService contactService;
    private final NotificationService notificationService;

    public SOSService() {
        this(new SOSRepository(), new LocationService(), new ContactService(), new NotificationService());
    }

    public SOSService(SOSRepository sosRepository,
                      LocationService locationService,
                      ContactService contactService,
                      NotificationService notificationService) {
        this.sosRepository = sosRepository;
        this.locationService = locationService;
        this.contactService = contactService;
        this.notificationService = notificationService;
    }

    public record SOSStartResult(boolean savedToDatabase, SOSRecord record,
                                 LocationRecord location, String locationMessage) {
    }

    public ServiceResult<LocationRecord> resolveLocationForSOS(User user, boolean locationSharingEnabled) {
        if (!locationSharingEnabled) {
            return ServiceResult.ok(null, "Location sharing disabled in App Settings.");
        }
        ServiceResult<LocationRecord> locResult = locationService.getCurrentLocation(user.getId());
        if (locResult.success() && locResult.data() != null) {
            LocationRecord loc = locResult.data();
            // Save snapshot to location_history if not yet persisted
            locationService.refreshAndRecordLocation(
                    user.getId(),
                    loc.getLatitude(),
                    loc.getLongitude(),
                    loc.getAddress(),
                    loc.getLocationType()
            );
            return ServiceResult.ok(loc, "Location captured (" + loc.getLocationType() + ").");
        }
        return ServiceResult.fail("Location unavailable.");
    }

    public NotificationService.NotificationResult dispatchNotifications(User user, UserSettings settings, LocationRecord location) {
        List<EmergencyContact> contacts = contactService.getContactsOrEmpty(user.getId());
        boolean enabled = settings == null || settings.isNotificationEnabled();
        return notificationService.sendEmergencyNotifications(user, contacts, location, enabled);
    }

    public ServiceResult<SOSRecord> saveSOSRecord(User user, LocationRecord location, String status) {
        Double lat = location != null ? location.getLatitude() : null;
        Double lng = location != null ? location.getLongitude() : null;
        String locType = location != null ? location.getLocationType() : LocationRecord.TYPE_UNAVAILABLE;
        try {
            SOSRecord record = sosRepository.create(
                    user.getId(),
                    status,
                    lat,
                    lng,
                    locType,
                    DateTimeUtils.nowIso()
            );
            return ServiceResult.ok(record, "SOS record saved to SQLite.");
        } catch (SQLException e) {
            return ServiceResult.fail("Failed to save SOS record to SQLite.");
        }
    }

    public ServiceResult<SOSRecord> cancelSOS(long sosId, long userId) {
        String cancelledAt = DateTimeUtils.nowIso();
        try {
            boolean updated = sosRepository.updateStatus(sosId, userId, SOSRecord.STATUS_CANCELLED, cancelledAt);
            if (!updated) {
                return ServiceResult.fail("SOS record not found.");
            }
            SOSRecord cancelled = new SOSRecord(sosId, userId, SOSRecord.STATUS_CANCELLED,
                    null, null, LocationRecord.TYPE_UNAVAILABLE, cancelledAt, cancelledAt);
            return ServiceResult.ok(cancelled, "SOS cancelled and updated in database.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error while cancelling SOS.");
        }
    }
}
