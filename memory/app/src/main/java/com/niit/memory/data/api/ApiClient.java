package com.niit.memory.data.api;

import android.content.Context;
import com.niit.memory.BuildConfig;
import com.niit.memory.util.SessionManager;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    static final String BASE_URL = "http://47.95.120.193:8081/";
    private static Retrofit retrofit;

    public static synchronized Retrofit getInstance(Context context) {
        if (retrofit == null) {
            SessionManager sessionManager = SessionManager.getInstance(context);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessionManager));

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                clientBuilder.addInterceptor(logging);
            }

            OkHttpClient client = clientBuilder
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
}
