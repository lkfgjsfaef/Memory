package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import retrofit2.Response;

public class DailyRepository {
    private final DailyService service;

    public DailyRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(DailyService.class);
    }

    public List<DailyRecord> getRecords(Integer year, Integer month) throws Exception {
        Response<ApiResponse<List<DailyRecord>>> resp = service.getRecords(year, month).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<DailyRecord>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public Map<String, Object> getStats() throws Exception {
        Response<ApiResponse<Map<String, Object>>> resp = service.getStats().execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Map<String, Object>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void createRecord(DailyRecord record) throws Exception {
        Response<ApiResponse<Void>> resp = service.createRecord(record).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    public DailyRecord updateRecord(long id, DailyRecord record) throws Exception {
        Response<ApiResponse<DailyRecord>> resp = service.updateRecord(id, record).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<DailyRecord> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void deleteRecord(long id) throws Exception {
        Response<ApiResponse<Void>> resp = service.deleteRecord(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }
}
