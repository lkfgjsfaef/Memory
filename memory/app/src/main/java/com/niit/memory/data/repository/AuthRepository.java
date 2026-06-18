package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.util.Map;

public class AuthRepository {
    private final AuthService service;

    public AuthRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(AuthService.class);
    }

    public LoginResponse login(String username, String password) throws Exception {
        return ApiResponse.parseResponse(service.login(new LoginRequest(username, password)).execute());
    }

    public User getMe() throws Exception {
        return ApiResponse.parseResponse(service.getMe().execute());
    }

    public User getUserById(long id) throws Exception {
        return ApiResponse.parseResponse(service.getUserById(id).execute());
    }

    public void updateAvatar(String avatarUrl) throws Exception {
        Map<String, String> body = new java.util.HashMap<>();
        body.put("avatarUrl", avatarUrl);
        ApiResponse.validateResponse(service.updateAvatar(body).execute());
    }
}
