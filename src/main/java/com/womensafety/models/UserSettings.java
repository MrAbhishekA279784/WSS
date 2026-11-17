package com.womensafety.models;

public class UserSettings {
    private long id;
    private long userId;
    private String theme;
    private boolean notificationEnabled;
    private boolean locationSharingEnabled;

    public UserSettings() {
        this.theme = "DARK";
        this.notificationEnabled = true;
        this.locationSharingEnabled = true;
    }

    public UserSettings(long id, long userId, String theme,
                        boolean notificationEnabled, boolean locationSharingEnabled) {
        this.id = id;
        this.userId = userId;
        this.theme = theme;
        this.notificationEnabled = notificationEnabled;
        this.locationSharingEnabled = locationSharingEnabled;
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public boolean isLocationSharingEnabled() {
        return locationSharingEnabled;
    }

    public void setLocationSharingEnabled(boolean locationSharingEnabled) {
        this.locationSharingEnabled = locationSharingEnabled;
    }
}
