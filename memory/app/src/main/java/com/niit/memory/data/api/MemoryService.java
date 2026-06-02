package com.niit.memory.data.api;

import com.niit.memory.data.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface MemoryService {
    // Albums
    @GET("api/albums")
    Call<ApiResponse<List<MemoryAlbum>>> getAlbums();

    @GET("api/albums/{id}")
    Call<ApiResponse<MemoryAlbum>> getAlbum(@Path("id") long id);

    @POST("api/albums")
    Call<ApiResponse<Void>> createAlbum(@Body MemoryAlbum album);

    @PUT("api/albums/{id}")
    Call<ApiResponse<MemoryAlbum>> updateAlbum(@Path("id") long id, @Body MemoryAlbum album);

    @DELETE("api/albums/{id}")
    Call<ApiResponse<Void>> deleteAlbum(@Path("id") long id);

    // Moments
    @GET("api/moments")
    Call<ApiResponse<List<TimelineGroup>>> getMoments();

    @GET("api/moments/{id}")
    Call<ApiResponse<MemoryMoment>> getMoment(@Path("id") long id);

    @POST("api/moments")
    Call<ApiResponse<Void>> createMoment(@Body MemoryMoment moment);

    @PUT("api/moments/{id}")
    Call<ApiResponse<MemoryMoment>> updateMoment(@Path("id") long id, @Body MemoryMoment moment);

    @DELETE("api/moments/{id}")
    Call<ApiResponse<Void>> deleteMoment(@Path("id") long id);

    // Locations
    @GET("api/locations")
    Call<ApiResponse<List<VisitedLocation>>> getLocations();

    @POST("api/locations")
    Call<ApiResponse<VisitedLocation>> createLocation(@Body VisitedLocation location);

    @PUT("api/locations/{id}")
    Call<ApiResponse<VisitedLocation>> updateLocation(@Path("id") long id, @Body VisitedLocation location);

    @DELETE("api/locations/{id}")
    Call<ApiResponse<Void>> deleteLocation(@Path("id") long id);
}
