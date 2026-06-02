package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.io.IOException;
import java.util.List;
import retrofit2.Response;

public class HomeRepository {
    private final HomeService service;
    private final Context context;

    public HomeRepository(Context context) {
        this.context = context;
        this.service = ApiClient.getInstance(context).create(HomeService.class);
    }

    public CoupleResponse getCouple() throws Exception {
        Response<ApiResponse<CoupleResponse>> resp = service.getCouple().execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<CoupleResponse> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public Couple updateCouple(Couple couple) throws Exception {
        Response<ApiResponse<Couple>> resp = service.updateCouple(couple).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Couple> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public List<ImportantDate> getImportantDates() throws Exception {
        Response<ApiResponse<List<ImportantDate>>> resp = service.getImportantDates().execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<ImportantDate>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void createImportantDate(ImportantDate date) throws Exception {
        Response<ApiResponse<Void>> resp = service.createImportantDate(date).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    public ImportantDate updateImportantDate(long id, ImportantDate date) throws Exception {
        Response<ApiResponse<ImportantDate>> resp = service.updateImportantDate(id, date).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<ImportantDate> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void deleteImportantDate(long id) throws Exception {
        Response<ApiResponse<Void>> resp = service.deleteImportantDate(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    // Calendar notes (delegates to CalendarService)
    public List<CalendarNote> getCalendarNotes(int year, int month) throws Exception {
        CalendarService calService = ApiClient.getInstance(context).create(CalendarService.class);
        Response<ApiResponse<List<CalendarNote>>> resp = calService.getNotes(year, month).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<CalendarNote>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void createCalendarNote(CalendarNote note) throws Exception {
        CalendarService calService = ApiClient.getInstance(context).create(CalendarService.class);
        Response<ApiResponse<Void>> resp = calService.createNote(note).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }
}
