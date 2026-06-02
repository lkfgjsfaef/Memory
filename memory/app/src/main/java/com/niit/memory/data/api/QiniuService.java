package com.niit.memory.data.api;

import com.niit.memory.data.model.ApiResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QiniuService {
    @GET("api/qiniu/upload-token")
    Call<ApiResponse<Map<String, String>>> getUploadToken();

    @DELETE("api/qiniu/delete-by-url")
    Call<ApiResponse<Void>> deleteByUrl(@Query("url") String url);
}
