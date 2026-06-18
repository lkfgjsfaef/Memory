package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.util.List;

public class CalendarRepository {
    private final CalendarService service;

    public CalendarRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(CalendarService.class);
    }

    public List<CalendarNote> getNotes(int year, int month) throws Exception {
        return ApiResponse.parseResponse(service.getNotes(year, month).execute());
    }

    public void createNote(CalendarNote note) throws Exception {
        ApiResponse.validateResponse(service.createNote(note).execute());
    }

    public CalendarNote updateNote(long id, CalendarNote note) throws Exception {
        return ApiResponse.parseResponse(service.updateNote(id, note).execute());
    }

    public void deleteNote(long id) throws Exception {
        ApiResponse.validateResponse(service.deleteNote(id).execute());
    }

    public List<CalendarMood> getMoods(int year, int month) throws Exception {
        return ApiResponse.parseResponse(service.getMoods(year, month).execute());
    }

    public CalendarMood upsertMood(CalendarMood mood) throws Exception {
        return ApiResponse.parseResponse(service.upsertMood(mood).execute());
    }
}
