package com.niit.memory.data.repository;

import android.content.Context;
import android.util.Log;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import retrofit2.Response;

public class WishRepository {
    private final WishService service;

    public WishRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(WishService.class);
    }

    public List<Wish> getWishes(String status, String category) throws Exception {
        Response<ApiResponse<List<Wish>>> resp = service.getWishes(status, category).execute();
        Log.d("WishRepository", "getWishes: httpCode=" + resp.code() + " status=" + status + " cat=" + category);
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<Wish>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        List<Wish> list = body.getData();
        if (list != null) {
            for (Wish w : list) {
                Log.d("WishRepository", "  wish: id=" + w.getId() + " title=" + w.getTitle()
                    + " imageUrls=" + w.getImageUrls());
            }
        }
        return list;
    }

    public Map<String, Object> getStats() throws Exception {
        Response<ApiResponse<Map<String, Object>>> resp = service.getStats().execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Map<String, Object>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void createWish(Wish wish) throws Exception {
        Response<ApiResponse<Void>> resp = service.createWish(wish).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    public Wish updateWish(long id, Wish wish) throws Exception {
        Response<ApiResponse<Wish>> resp = service.updateWish(id, wish).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Wish> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void updateStatus(long id, String status) throws Exception {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", status);
        Response<ApiResponse<Void>> resp = service.updateStatus(id, body).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> apiResp = resp.body();
        if (apiResp == null || !apiResp.isSuccess()) throw new Exception(apiResp != null ? apiResp.getMessage() : "更新失败");
    }

    public void deleteWish(long id) throws Exception {
        Response<ApiResponse<Void>> resp = service.deleteWish(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }
}
