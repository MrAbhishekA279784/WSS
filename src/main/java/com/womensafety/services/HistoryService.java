package com.womensafety.services;

import com.womensafety.models.SOSRecord;
import com.womensafety.repositories.SOSRepository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class HistoryService {

    private final SOSRepository sosRepository;

    public HistoryService() {
        this(new SOSRepository());
    }

    public HistoryService(SOSRepository sosRepository) {
        this.sosRepository = sosRepository;
    }

    public ServiceResult<List<SOSRecord>> getSOSHistory(long userId) {
        try {
            return ServiceResult.ok(sosRepository.findByUserId(userId));
        } catch (SQLException e) {
            return ServiceResult.fail("Failed to load SOS history from SQLite.");
        }
    }

    public List<SOSRecord> getSOSHistoryOrEmpty(long userId) {
        try {
            return sosRepository.findByUserId(userId);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
