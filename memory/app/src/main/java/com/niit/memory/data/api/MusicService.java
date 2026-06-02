package com.niit.memory.data.api;

import com.niit.memory.data.model.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface MusicService {
    @GET("api/music/search")
    Call<ApiResponse<List<Song>>> search(@Query("keyword") String keyword, @Query("limit") int limit);

    @GET("api/music/playlist")
    Call<ApiResponse<List<Song>>> getPlaylist();

    @PUT("api/music/playlist")
    Call<ApiResponse<Void>> savePlaylist(@Body List<Map<String, Object>> songs);
}
