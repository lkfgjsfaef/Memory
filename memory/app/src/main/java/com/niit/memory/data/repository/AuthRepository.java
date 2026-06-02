package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.io.IOException;
import java.util.Map;
import retrofit2.Response;

public class AuthRepository {
    private final AuthService service;
    private final Context context;

    public AuthRepository(Context context) {
        this.context = context;
        this.service = ApiClient.getInstance(context).create(AuthService.class);
    }

    public LoginResponse login(String username, String password) throws Exception {
        Response<ApiResponse<LoginResponse>> resp = service.login(new LoginRequest(username, password)).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error: " + resp.code());
        ApiResponse<LoginResponse> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "登录失败");
        return body.getData();
    }

    public User getMe() throws Exception {
        Response<ApiResponse<User>> resp = service.getMe().execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<User> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public User getUserById(long id) throws Exception {
        Response<ApiResponse<User>> resp = service.getUserById(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<User> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void updateAvatar(String avatarUrl) throws Exception {
        Map<String, String> body = new java.util.HashMap<>();
        body.put("avatarUrl", avatarUrl);
        Response<ApiResponse<Void>> resp = service.updateAvatar(body).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> apiResp = resp.body();
        if (apiResp == null || !apiResp.isSuccess()) throw new Exception(apiResp != null ? apiResp.getMessage() : "更新失败");
    }
}
