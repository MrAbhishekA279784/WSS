package com.womensafety.models;

public class EmergencyContact {
    private long id;
    private long userId;
    private String contactName;
    private String phoneNumber;
    private String relationship;
    private String createdAt;

    public EmergencyContact() {
    }

    public EmergencyContact(long id, long userId, String contactName, String phoneNumber,
                            String relationship, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
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

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getInitial() {
        if (contactName == null || contactName.isBlank()) {
            return "?";
        }
        return String.valueOf(Character.toUpperCase(contactName.trim().charAt(0)));
    }
}
