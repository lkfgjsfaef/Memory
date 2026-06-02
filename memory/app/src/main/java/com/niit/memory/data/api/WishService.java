package com.niit.memory.data.api;

import com.niit.memory.data.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface WishService {
    @GET("api/wishes")
    Call<ApiResponse<List<Wish>>> getWishes(@Query("status") String status, @Query("category") String category);

    @GET("api/wishes/stats")
    Call<ApiResponse<java.util.Map<String, Object>>> getStats();

    @POST("api/wishes")
    Call<ApiResponse<Void>> createWish(@Body Wish wish);

    @PUT("api/wishes/{id}")
    Call<ApiResponse<Wish>> updateWish(@Path("id") long id, @Body Wish wish);

    @PATCH("api/wishes/{id}/status")
    Call<ApiResponse<Void>> updateStatus(@Path("id") long id, @Body java.util.Map<String, String> body);

    @DELETE("api/wishes/{id}")
    Call<ApiResponse<Void>> deleteWish(@Path("id") long id);
}
