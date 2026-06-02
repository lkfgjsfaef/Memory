package com.niit.memory.data.api;

import com.niit.memory.data.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface CalendarService {
    @GET("api/calendar/notes")
    Call<ApiResponse<List<CalendarNote>>> getNotes(@Query("year") int year, @Query("month") int month);

    @POST("api/calendar/notes")
    Call<ApiResponse<Void>> createNote(@Body CalendarNote note);

    @PUT("api/calendar/notes/{id}")
    Call<ApiResponse<CalendarNote>> updateNote(@Path("id") long id, @Body CalendarNote note);

    @DELETE("api/calendar/notes/{id}")
    Call<ApiResponse<Void>> deleteNote(@Path("id") long id);

    @GET("api/calendar/moods")
    Call<ApiResponse<List<CalendarMood>>> getMoods(@Query("year") int year, @Query("month") int month);

    @POST("api/calendar/moods")
    Call<ApiResponse<CalendarMood>> upsertMood(@Body CalendarMood mood);
}
