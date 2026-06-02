package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.io.IOException;
import java.util.List;
import retrofit2.Response;

public class CalendarRepository {
    private final CalendarService service;

    public CalendarRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(CalendarService.class);
    }

    public List<CalendarNote> getNotes(int year, int month) throws Exception {
        Response<ApiResponse<List<CalendarNote>>> resp = service.getNotes(year, month).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<CalendarNote>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void createNote(CalendarNote note) throws Exception {
        Response<ApiResponse<Void>> resp = service.createNote(note).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    public CalendarNote updateNote(long id, CalendarNote note) throws Exception {
        Response<ApiResponse<CalendarNote>> resp = service.updateNote(id, note).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<CalendarNote> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void deleteNote(long id) throws Exception {
        Response<ApiResponse<Void>> resp = service.deleteNote(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    public List<CalendarMood> getMoods(int year, int month) throws Exception {
        Response<ApiResponse<List<CalendarMood>>> resp = service.getMoods(year, month).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<CalendarMood>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public CalendarMood upsertMood(CalendarMood mood) throws Exception {
        Response<ApiResponse<CalendarMood>> resp = service.upsertMood(mood).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<CalendarMood> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }
}
