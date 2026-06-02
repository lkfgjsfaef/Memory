package com.niit.memory.data.api;

import com.niit.memory.data.model.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface AuthService {
    @POST("api/auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @GET("api/auth/me")
    Call<ApiResponse<User>> getMe();

    @GET("api/auth/user/{id}")
    Call<ApiResponse<User>> getUserById(@Path("id") long id);

    @PUT("api/auth/avatar")
    Call<ApiResponse<Void>> updateAvatar(@Body java.util.Map<String, String> body);

    @GET("api/auth/avatars")
    Call<ApiResponse<AvatarsInfo>> getAvatars();
}
