package com.womensafety.services;

import com.womensafety.models.EmergencyContact;
import com.womensafety.repositories.ContactRepository;
import com.womensafety.utils.DateTimeUtils;
import com.womensafety.utils.ValidationUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService() {
        this(new ContactRepository());
    }

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public ServiceResult<List<EmergencyContact>> getContacts(long userId) {
        try {
            return ServiceResult.ok(contactRepository.findByUserId(userId));
        } catch (SQLException e) {
            return ServiceResult.fail("Unable to load trusted contacts.");
        }
    }

    public List<EmergencyContact> getContactsOrEmpty(long userId) {
        try {
            return contactRepository.findByUserId(userId);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public ServiceResult<EmergencyContact> addContact(long userId, String name, String phoneNumber, String relationship) {
        if (!ValidationUtils.isValidName(name)) {
            return ServiceResult.fail("Contact name must be at least 2 characters.");
        }
        if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
            return ServiceResult.fail("Please enter a valid phone number (7 to 15 digits).");
        }

        String normalizedDigits = phoneNumber.replaceAll("[^0-9]", "");
        try {
            if (contactRepository.existsByUserAndPhone(userId, normalizedDigits, null)) {
                return ServiceResult.fail("A trusted contact with this phone number already exists.");
            }
            EmergencyContact saved = contactRepository.create(
                    userId, name, phoneNumber, relationship, DateTimeUtils.nowIso());
            return ServiceResult.ok(saved, "Trusted contact added.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error while adding contact.");
        }
    }

    public ServiceResult<Void> updateContact(long contactId, long userId, String name, String phoneNumber, String relationship) {
        if (!ValidationUtils.isValidName(name)) {
            return ServiceResult.fail("Contact name must be at least 2 characters.");
        }
        if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
            return ServiceResult.fail("Please enter a valid phone number (7 to 15 digits).");
        }

        String normalizedDigits = phoneNumber.replaceAll("[^0-9]", "");
        try {
            if (contactRepository.existsByUserAndPhone(userId, normalizedDigits, contactId)) {
                return ServiceResult.fail("Another trusted contact already uses this phone number.");
            }
            boolean updated = contactRepository.update(contactId, userId, name, phoneNumber, relationship);
            return updated
                    ? ServiceResult.ok(null, "Contact updated.")
                    : ServiceResult.fail("Contact not found.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error while updating contact.");
        }
    }

    public ServiceResult<Void> deleteContact(long contactId, long userId) {
        try {
            boolean deleted = contactRepository.delete(contactId, userId);
            return deleted
                    ? ServiceResult.ok(null, "Contact removed.")
                    : ServiceResult.fail("Contact not found.");
        } catch (SQLException e) {
            return ServiceResult.fail("Database error while deleting contact.");
        }
    }
}
