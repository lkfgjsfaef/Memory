package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.util.List;
import java.util.Map;

public class WishRepository {
    private final WishService service;

    public WishRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(WishService.class);
    }

    public List<Wish> getWishes(String status, String category) throws Exception {
        return ApiResponse.parseResponse(service.getWishes(status, category).execute());
    }

    public Map<String, Object> getStats() throws Exception {
        return ApiResponse.parseResponse(service.getStats().execute());
    }

    public void createWish(Wish wish) throws Exception {
        ApiResponse.validateResponse(service.createWish(wish).execute());
    }

    public Wish updateWish(long id, Wish wish) throws Exception {
        return ApiResponse.parseResponse(service.updateWish(id, wish).execute());
    }

    public void updateStatus(long id, String status) throws Exception {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", status);
        ApiResponse.validateResponse(service.updateStatus(id, body).execute());
    }

    public void deleteWish(long id) throws Exception {
        ApiResponse.validateResponse(service.deleteWish(id).execute());
    }
}
