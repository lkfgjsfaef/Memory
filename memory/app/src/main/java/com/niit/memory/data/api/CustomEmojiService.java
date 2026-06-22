package com.niit.memory.data.api;

import com.niit.memory.data.model.ApiResponse;
import com.niit.memory.data.model.CustomEmoji;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface CustomEmojiService {
    @GET("api/custom-emojis")
    Call<ApiResponse<List<CustomEmoji>>> list();

    @POST("api/custom-emojis")
    Call<ApiResponse<Void>> add(@Body CustomEmoji emoji);

    @DELETE("api/custom-emojis/{id}")
    Call<ApiResponse<Void>> delete(@Path("id") long id);
}
