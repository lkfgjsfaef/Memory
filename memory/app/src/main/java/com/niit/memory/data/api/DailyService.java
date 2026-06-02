package com.niit.memory.data.api;

import com.niit.memory.data.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface DailyService {
    @GET("api/daily-records")
    Call<ApiResponse<List<DailyRecord>>> getRecords(@Query("year") Integer year, @Query("month") Integer month);

    @GET("api/daily-records/stats")
    Call<ApiResponse<java.util.Map<String, Object>>> getStats();

    @POST("api/daily-records")
    Call<ApiResponse<Void>> createRecord(@Body DailyRecord record);

    @PUT("api/daily-records/{id}")
    Call<ApiResponse<DailyRecord>> updateRecord(@Path("id") long id, @Body DailyRecord record);

    @DELETE("api/daily-records/{id}")
    Call<ApiResponse<Void>> deleteRecord(@Path("id") long id);
}
