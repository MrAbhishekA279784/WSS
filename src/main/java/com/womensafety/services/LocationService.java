package com.womensafety.services;

import com.womensafety.config.AppConfig;
import com.womensafety.models.LocationRecord;
import com.womensafety.repositories.LocationRepository;
import com.womensafety.utils.DateTimeUtils;
import com.womensafety.utils.MapUtils;
import com.womensafety.utils.ValidationUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Provides current location snapshot and Google Maps URL generation.
 * Since standard desktop JVMs lack hardware GPS receivers, coordinates default
 * to a clearly labelled SIMULATED mode unless updated manually by the user.
 */
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService() {
        this(new LocationRepository());
    }

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public ServiceResult<LocationRecord> getCurrentLocation(long userId) {
        try {
            Optional<LocationRecord> latest = locationRepository.findLatestByUserId(userId);
            if (latest.isPresent()) {
                return ServiceResult.ok(latest.get(), "Loaded saved location (" + latest.get().getLocationType() + ").");
            }
            LocationRecord simulated = new LocationRecord(
                    0,
                    userId,
                    AppConfig.DEFAULT_SIMULATED_LAT,
                    AppConfig.DEFAULT_SIMULATED_LNG,
                    AppConfig.DEFAULT_SIMULATED_ADDRESS,
                    LocationRecord.TYPE_SIMULATED,
                    DateTimeUtils.nowIso()
            );
            return ServiceResult.ok(simulated, "Hardware GPS unavailable — using simulated location.");
        } catch (SQLException e) {
            return ServiceResult.fail("Unable to read location history from SQLite.");
        }
    }

    public ServiceResult<LocationRecord> refreshAndRecordLocation(long userId, double latitude, double longitude,
                                                                  String address, String locationType) {
        if (!ValidationUtils.isValidCoordinates(latitude, longitude)) {
            return ServiceResult.fail("Coordinates must be within valid latitude (-90..90) and longitude (-180..180) bounds.");
        }
        String safeType = (locationType == null || locationType.isBlank())
                ? LocationRecord.TYPE_SIMULATED
                : locationType;
        String safeAddress = (address == null || address.isBlank())
                ? "Simulated Coordinates (" + MapUtils.formatCoordinates(latitude, longitude) + ")"
                : address.trim();
        try {
            LocationRecord saved = locationRepository.save(
                    userId, latitude, longitude, safeAddress, safeType, DateTimeUtils.nowIso());
            return ServiceResult.ok(saved, "Location snapshot saved.");
        } catch (SQLException e) {
            return ServiceResult.fail("Failed to save location snapshot.");
        }
    }

    public ServiceResult<List<LocationRecord>> getLocationHistory(long userId) {
        try {
            return ServiceResult.ok(locationRepository.findByUserId(userId));
        } catch (SQLException e) {
            return ServiceResult.fail("Unable to fetch location history.");
        }
    }

    public String getShareableGoogleMapsUrl(LocationRecord record) {
        return MapUtils.buildGoogleMapsUrl(record.getLatitude(), record.getLongitude());
    }
}
