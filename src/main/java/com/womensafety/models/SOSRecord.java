package com.womensafety.models;

public class SOSRecord {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private long id;
    private long userId;
    private String status;
    private Double latitude;
    private Double longitude;
    private String locationType;
    private String createdAt;
    private String cancelledAt;

    public SOSRecord() {
    }

    public SOSRecord(long id, long userId, String status, Double latitude, Double longitude,
                     String locationType, String createdAt, String cancelledAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationType = locationType;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(String cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equalsIgnoreCase(status);
    }

    public String getDisplayTitle() {
        return isCancelled() ? "SOS Cancelled" : "SOS Alert";
    }
}
