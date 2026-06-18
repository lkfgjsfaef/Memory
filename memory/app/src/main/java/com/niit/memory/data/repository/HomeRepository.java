package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.util.List;

public class HomeRepository {
    private final HomeService service;
    private final Context context;

    public HomeRepository(Context context) {
        this.context = context;
        this.service = ApiClient.getInstance(context).create(HomeService.class);
    }

    public CoupleResponse getCouple() throws Exception {
        return ApiResponse.parseResponse(service.getCouple().execute());
    }

    public Couple updateCouple(Couple couple) throws Exception {
        return ApiResponse.parseResponse(service.updateCouple(couple).execute());
    }

    public List<ImportantDate> getImportantDates() throws Exception {
        return ApiResponse.parseResponse(service.getImportantDates().execute());
    }

    public void createImportantDate(ImportantDate date) throws Exception {
        ApiResponse.validateResponse(service.createImportantDate(date).execute());
    }

    public ImportantDate updateImportantDate(long id, ImportantDate date) throws Exception {
        return ApiResponse.parseResponse(service.updateImportantDate(id, date).execute());
    }

    public void deleteImportantDate(long id) throws Exception {
        ApiResponse.validateResponse(service.deleteImportantDate(id).execute());
    }

    public List<CalendarNote> getCalendarNotes(int year, int month) throws Exception {
        CalendarService calService = ApiClient.getInstance(context).create(CalendarService.class);
        return ApiResponse.parseResponse(calService.getNotes(year, month).execute());
    }

    public void createCalendarNote(CalendarNote note) throws Exception {
        CalendarService calService = ApiClient.getInstance(context).create(CalendarService.class);
        ApiResponse.validateResponse(calService.createNote(note).execute());
    }
}
