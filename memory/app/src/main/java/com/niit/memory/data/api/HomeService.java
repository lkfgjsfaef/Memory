package com.niit.memory.data.api;

import com.niit.memory.data.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface HomeService {
    @GET("api/couple")
    Call<ApiResponse<CoupleResponse>> getCouple();

    @PUT("api/couple")
    Call<ApiResponse<Couple>> updateCouple(@Body Couple couple);

    @GET("api/important-dates")
    Call<ApiResponse<List<ImportantDate>>> getImportantDates();

    @POST("api/important-dates")
    Call<ApiResponse<Void>> createImportantDate(@Body ImportantDate date);

    @PUT("api/important-dates/{id}")
    Call<ApiResponse<ImportantDate>> updateImportantDate(@Path("id") long id, @Body ImportantDate date);

    @DELETE("api/important-dates/{id}")
    Call<ApiResponse<Void>> deleteImportantDate(@Path("id") long id);
}
