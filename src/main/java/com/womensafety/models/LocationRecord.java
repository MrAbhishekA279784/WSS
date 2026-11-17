package com.womensafety.models;

public class LocationRecord {
    public static final String TYPE_SIMULATED = "SIMULATED";
    public static final String TYPE_MANUAL = "MANUAL";
    public static final String TYPE_UNAVAILABLE = "UNAVAILABLE";

    private long id;
    private long userId;
    private double latitude;
    private double longitude;
    private String address;
    private String locationType;
    private String createdAt;

    public LocationRecord() {
    }

    public LocationRecord(long id, long userId, double latitude, double longitude,
                          String address, String locationType, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.locationType = locationType;
        this.createdAt = createdAt;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
}
