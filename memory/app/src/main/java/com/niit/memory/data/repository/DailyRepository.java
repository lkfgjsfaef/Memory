package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.util.List;
import java.util.Map;

public class DailyRepository {
    private final DailyService service;

    public DailyRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(DailyService.class);
    }

    public List<DailyRecord> getRecords(Integer year, Integer month) throws Exception {
        return ApiResponse.parseResponse(service.getRecords(year, month).execute());
    }

    public Map<String, Object> getStats() throws Exception {
        return ApiResponse.parseResponse(service.getStats().execute());
    }

    public void createRecord(DailyRecord record) throws Exception {
        ApiResponse.validateResponse(service.createRecord(record).execute());
    }

    public DailyRecord updateRecord(long id, DailyRecord record) throws Exception {
        return ApiResponse.parseResponse(service.updateRecord(id, record).execute());
    }

    public void deleteRecord(long id) throws Exception {
        ApiResponse.validateResponse(service.deleteRecord(id).execute());
    }
}
